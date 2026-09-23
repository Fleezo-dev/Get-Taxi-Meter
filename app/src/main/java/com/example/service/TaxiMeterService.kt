package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.R
import com.example.TaxiMeterApplication
import com.example.data.TripEntity
import com.example.data.RideMode
import com.example.data.TripAssignmentRepository
import com.example.engine.MeterEngine
import com.example.model.ExtraCharge
import com.example.model.Tariff
import com.example.model.TripState
import com.example.model.TripStatus
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

class TaxiMeterService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tickerJob: Job? = null
    private var dbSyncJob: Job? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var wakeLock: PowerManager.WakeLock? = null

    // Tracking state
    private var lastAcceptedLat: Double? = null
    private var lastAcceptedLon: Double? = null
    private var lastAcceptedTimeMs: Long? = null
    private var motionState = MeterEngine.MotionState(isMoving = false, consecutiveStopTicks = 0, consecutiveMoveTicks = 0)
    private var lastDbSyncTimeMs: Long = 0L
    private val tripAssignmentRepository = TripAssignmentRepository()
    private var activeAssignmentId: String? = null

    override fun onCreate() {
        super.onCreate()
        _isServiceRunning.value = true
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Android may recreate a START_STICKY foreground service with a null intent
        // after the app process is killed. If an active trip exists in Room, restore
        // it automatically instead of starting with an empty meter.
        if (intent == null) {
            serviceScope.launch(Dispatchers.IO) {
                val activeTrip = TaxiMeterApplication.instance.database.tripDao().getActiveTrip()
                if (activeTrip != null && _tripState.value.status == TripStatus.READY) {
                    resumeTripInternal(activeTrip.tripId)
                }
            }
            return START_STICKY
        }
        when (intent.action) {
            ACTION_START_TRIP -> {
                activeAssignmentId = null
                val app = TaxiMeterApplication.instance
                val cityTariff = app.tariffRepository.loadTariff()
                val pricing = app.rideModeRepository.pricing.value
                val tariff = when (app.rideModeRepository.selectedMode.value) {
                    RideMode.CITY_RIDE -> cityTariff
                    RideMode.HOURLY_RENTAL -> cityTariff.copy(
                        id = "hourly_rental",
                        name = "Hourly Rental",
                        baseFare = pricing.hourlyRate,
                        minimumFare = 0.0,
                        distanceRatePerKm = pricing.hourlyExtraKmRate,
                        waitingRatePerMinute = 0.0,
                        freeDistanceKm = pricing.hourlyFreeKm,
                        freeWaitingMinutes = 0.0,
                        nightSurchargeMultiplier = 1.0
                    )
                    RideMode.OUTSTATION -> cityTariff.copy(
                        id = "outstation",
                        name = "Outstation",
                        baseFare = pricing.outstationDriverBata,
                        minimumFare = 0.0,
                        distanceRatePerKm = pricing.outstationPerKmRate,
                        waitingRatePerMinute = 0.0,
                        freeDistanceKm = 0.0,
                        freeWaitingMinutes = 0.0,
                        nightSurchargeMultiplier = 1.0
                    )
                }
                startTripInternal(tariff)
            }
            ACTION_START_LOADED_TRIP -> {
                activeAssignmentId = intent.getStringExtra(EXTRA_ASSIGNMENT_ID)
                val app = TaxiMeterApplication.instance
                val mode = runCatching {
                    RideMode.valueOf(intent.getStringExtra(EXTRA_RIDE_MODE) ?: RideMode.CITY_RIDE.name)
                }.getOrDefault(RideMode.CITY_RIDE)
                val cityTariff = app.tariffRepository.loadTariff()
                val pricing = app.rideModeRepo.pricing.value
                val tariff = when (mode) {
                    RideMode.CITY_RIDE -> cityTariff
                    RideMode.HOURLY_RENTAL -> cityTariff.copy(
                        id = "hourly_rental",
                        name = "Hourly Rental",
                        baseFare = pricing.hourlyRate,
                        minimumFare = 0.0,
                        distanceRatePerKm = pricing.hourlyExtraKmRate,
                        waitingRatePerMinute = 0.0,
                        freeDistanceKm = pricing.hourlyFreeKm,
                        freeWaitingMinutes = 0.0,
                        nightSurchargeMultiplier = 1.0
                    )
                    RideMode.OUTSTATION -> cityTariff.copy(
                        id = "outstation",
                        name = "Outstation",
                        baseFare = pricing.outstationDriverBata,
                        minimumFare = 0.0,
                        distanceRatePerKm = pricing.outstationPerKmRate,
                        waitingRatePerMinute = 0.0,
                        freeDistanceKm = 0.0,
                        freeWaitingMinutes = 0.0,
                        nightSurchargeMultiplier = 1.0
                    )
                }
                startTripInternal(tariff)
                activeAssignmentId?.let { assignmentId ->
                    serviceScope.launch(Dispatchers.IO) {
                        tripAssignmentRepository.markStarted(assignmentId)
                            .onFailure { android.util.Log.w("TripAssignment", "Unable to mark trip STARTED", it) }
                    }
                }
            }
            ACTION_RESUME_RECOVERED -> {
                val tripId = intent.getLongExtra(EXTRA_TRIP_ID, 0L)
                resumeTripInternal(tripId)
            }
            ACTION_ADD_EXTRA -> {
                val label = intent.getStringExtra(EXTRA_LABEL) ?: "Extra"
                val amount = intent.getDoubleExtra(EXTRA_AMOUNT, 0.0)
                addExtraInternal(label, amount)
            }
            ACTION_REMOVE_EXTRA -> {
                val extraId = intent.getStringExtra(EXTRA_ID) ?: ""
                removeExtraInternal(extraId)
            }
            ACTION_END_TRIP -> {
                endTripInternal()
            }
        }
        return START_STICKY
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TaxiMeter::ServiceWakeLock").apply {
                acquire(12 * 60 * 60 * 1000L) // 12 hours max safety
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startTripInternal(tariff: Tariff) {
        val now = System.currentTimeMillis()
        val initialBreakdown = MeterEngine.calculateFare(0.0, 0L, emptyList(), tariff, 0L)

        val initialState = TripState(
            tripId = 0L,
            status = TripStatus.ACTIVE,
            startTimestamp = now,
            currentTimestamp = now,
            totalDistanceMeters = 0.0,
            movingDistanceMeters = 0.0,
            waitingDurationSeconds = 0L,
            tripDurationSeconds = 0L,
            currentSpeedKmH = 0f,
            isMoving = false,
            gpsAccuracyMeters = 0f,
            gpsStatus = "Acquiring GPS...",
            lastGpsTimestamp = 0L,
            breakdown = initialBreakdown,
            extras = emptyList(),
            tariff = tariff,
            isRecovered = false
        )

        _tripState.value = initialState
        startForegroundWithNotification(initialState)

        // Persist initial trip record to database
        serviceScope.launch(Dispatchers.IO) {
            val entity = TripEntity.fromTripState(initialState)
            val assignedId = TaxiMeterApplication.instance.database.tripDao().insertTrip(entity)
            _tripState.value = _tripState.value.copy(tripId = assignedId)
        }

        startLocationTracking()
        startTicker()
    }

    private fun resumeTripInternal(tripId: Long) {
        serviceScope.launch(Dispatchers.IO) {
            val entity = if (tripId > 0) {
                TaxiMeterApplication.instance.database.tripDao().getTripById(tripId)
            } else {
                TaxiMeterApplication.instance.database.tripDao().getActiveTrip()
            }

            if (entity != null) {
                val now = System.currentTimeMillis()
                val recoveredState = entity.toTripState().copy(
                    status = TripStatus.ACTIVE,
                    currentTimestamp = now,
                    gpsStatus = "Resumed active trip",
                    isRecovered = true
                )

                _tripState.value = recoveredState
                lastAcceptedLat = recoveredState.currentLatitude
                lastAcceptedLon = recoveredState.currentLongitude
                lastAcceptedTimeMs = recoveredState.lastGpsTimestamp

                launch(Dispatchers.Main) {
                    startForegroundWithNotification(recoveredState)
                    startLocationTracking()
                    startTicker()
                }
            } else {
                // No trip to recover
                stopSelf()
            }
        }
    }

    private fun startForegroundWithNotification(state: TripState) {
        val notification = buildNotification(state)
        val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        } else 0

        ServiceCompat.startForeground(this, TaxiMeterApplication.NOTIFICATION_ID, notification, foregroundServiceType)

        if (FloatingOverlayManager.isOverlayEnabled(this) && FloatingOverlayManager.canDrawOverlay(this)) {
            FloatingOverlayManager.showOverlay(this)
        }
    }

    private fun buildNotification(state: TripState): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val durationFormatted = formatDuration(state.tripDurationSeconds)
        val distanceKmFormatted = String.format(Locale.US, "%.2f km", state.distanceKm)
        val fareFormatted = String.format(Locale.US, "₹%.2f", state.breakdown.totalFare)
        val motionStatus = if (state.isMoving) "MOVING" else "WAITING"

        val title = "Get Taxi Meter • $fareFormatted"
        val contentText = "$distanceKmFormatted • $durationFormatted • $motionStatus"
        val subText = "Active Fare"

        return NotificationCompat.Builder(this, TaxiMeterApplication.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSubText(subText)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(android.R.drawable.ic_menu_view, "Open Meter", pendingIntent)
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationTracking() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateIntervalMillis(800L)
            .setMinUpdateDistanceMeters(1f)
            .setWaitForAccurateLocation(false)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                handleNewLocation(location)
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                mainLooper
            )
        } catch (e: Exception) {
            e.printStackTrace()
            _tripState.value = _tripState.value.copy(gpsStatus = "GPS permission error")
        }
    }

    private fun handleNewLocation(location: Location) {
        val now = System.currentTimeMillis()
        val accuracy = if (location.hasAccuracy()) location.accuracy else 50f
        val speedKmh = if (location.hasSpeed()) location.speed * 3.6f else 0f

        val filterResult = MeterEngine.filterLocation(
            newLat = location.latitude,
            newLon = location.longitude,
            newAccuracy = accuracy,
            newTimeMs = location.time,
            lastLat = lastAcceptedLat,
            lastLon = lastAcceptedLon,
            lastTimeMs = lastAcceptedTimeMs,
            maxAccuracyAllowedMeters = 35f,
            maxSpeedAllowedMetersPerSec = 50.0
        )

        val currentVal = _tripState.value
        val speedThreshold = currentVal.tariff.waitingSpeedThresholdKmH.toFloat()
        motionState = MeterEngine.evaluateMotion(speedKmh, speedThreshold, motionState)

        if (filterResult.isAccepted) {
            val deltaMeters = filterResult.distanceDeltaMeters
            val newTotalDistance = currentVal.totalDistanceMeters + deltaMeters
            val newMovingDistance = if (motionState.isMoving) {
                currentVal.movingDistanceMeters + deltaMeters
            } else {
                currentVal.movingDistanceMeters
            }

            lastAcceptedLat = location.latitude
            lastAcceptedLon = location.longitude
            lastAcceptedTimeMs = location.time

            val startLat = currentVal.startLatitude ?: location.latitude
            val startLon = currentVal.startLongitude ?: location.longitude

            val updatedBreakdown = MeterEngine.calculateFare(
                distanceMeters = newTotalDistance,
                waitingDurationSeconds = currentVal.waitingDurationSeconds,
                extras = currentVal.extras,
                tariff = currentVal.tariff,
                tripDurationSeconds = currentVal.tripDurationSeconds
            )

            val gpsDesc = if (accuracy <= 10f) {
                "GPS Strong (±${accuracy.toInt()}m)"
            } else {
                "GPS OK (±${accuracy.toInt()}m)"
            }

            _tripState.value = currentVal.copy(
                currentTimestamp = now,
                totalDistanceMeters = newTotalDistance,
                movingDistanceMeters = newMovingDistance,
                currentSpeedKmH = speedKmh,
                isMoving = motionState.isMoving,
                gpsAccuracyMeters = accuracy,
                gpsStatus = gpsDesc,
                lastGpsTimestamp = location.time,
                startLatitude = startLat,
                startLongitude = startLon,
                currentLatitude = location.latitude,
                currentLongitude = location.longitude,
                breakdown = updatedBreakdown
            )
        } else {
            // Update speed, status, accuracy even if coordinate jump was rejected
            _tripState.value = currentVal.copy(
                currentTimestamp = now,
                currentSpeedKmH = speedKmh,
                isMoving = motionState.isMoving,
                gpsAccuracyMeters = accuracy,
                gpsStatus = "Filtered: ${filterResult.reason}"
            )
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            while (isActive) {
                delay(1000L)
                val currentVal = _tripState.value
                if (currentVal.status != TripStatus.ACTIVE && currentVal.status != TripStatus.WAITING) {
                    continue
                }

                val now = System.currentTimeMillis()
                val newTripDuration = currentVal.tripDurationSeconds + 1
                val newWaitingDuration = if (!currentVal.isMoving) {
                    currentVal.waitingDurationSeconds + 1
                } else {
                    currentVal.waitingDurationSeconds
                }

                val newStatus = if (currentVal.isMoving) TripStatus.ACTIVE else TripStatus.WAITING

                val updatedBreakdown = MeterEngine.calculateFare(
                    distanceMeters = currentVal.totalDistanceMeters,
                    waitingDurationSeconds = newWaitingDuration,
                    extras = currentVal.extras,
                    tariff = currentVal.tariff,
                    tripDurationSeconds = newTripDuration
                )

                val updatedState = currentVal.copy(
                    status = newStatus,
                    currentTimestamp = now,
                    tripDurationSeconds = newTripDuration,
                    waitingDurationSeconds = newWaitingDuration,
                    breakdown = updatedBreakdown
                )
                _tripState.value = updatedState

                // Update ongoing notification
                val notification = buildNotification(updatedState)
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                notificationManager.notify(TaxiMeterApplication.NOTIFICATION_ID, notification)

                // Periodic database sync every 3 seconds
                if (now - lastDbSyncTimeMs >= 3000L && updatedState.tripId > 0) {
                    lastDbSyncTimeMs = now
                    syncTripToDatabase(updatedState)
                }
            }
        }
    }

    private fun syncTripToDatabase(state: TripState) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                val entity = TripEntity.fromTripState(state)
                TaxiMeterApplication.instance.database.tripDao().updateTrip(entity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun addExtraInternal(label: String, amount: Double) {
        val currentVal = _tripState.value
        val newExtra = ExtraCharge(
            id = UUID.randomUUID().toString(),
            label = label,
            amount = amount
        )
        val updatedExtras = currentVal.extras + newExtra
        val updatedBreakdown = MeterEngine.calculateFare(
            distanceMeters = currentVal.totalDistanceMeters,
            waitingDurationSeconds = currentVal.waitingDurationSeconds,
            extras = updatedExtras,
            tariff = currentVal.tariff,
            tripDurationSeconds = currentVal.tripDurationSeconds
        )
        val updatedState = currentVal.copy(
            extras = updatedExtras,
            breakdown = updatedBreakdown
        )
        _tripState.value = updatedState
        syncTripToDatabase(updatedState)
    }

    private fun removeExtraInternal(extraId: String) {
        val currentVal = _tripState.value
        val updatedExtras = currentVal.extras.filterNot { it.id == extraId }
        val updatedBreakdown = MeterEngine.calculateFare(
            distanceMeters = currentVal.totalDistanceMeters,
            waitingDurationSeconds = currentVal.waitingDurationSeconds,
            extras = updatedExtras,
            tariff = currentVal.tariff
        )
        val updatedState = currentVal.copy(
            extras = updatedExtras,
            breakdown = updatedBreakdown
        )
        _tripState.value = updatedState
        syncTripToDatabase(updatedState)
    }

    private fun endTripInternal() {
        tickerJob?.cancel()
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }

        val now = System.currentTimeMillis()
        val currentVal = _tripState.value
        val finalBreakdown = MeterEngine.calculateFare(
            distanceMeters = currentVal.totalDistanceMeters,
            waitingDurationSeconds = currentVal.waitingDurationSeconds,
            extras = currentVal.extras,
            tariff = currentVal.tariff,
            tripDurationSeconds = currentVal.tripDurationSeconds
        )

        val completedState = currentVal.copy(
            status = TripStatus.COMPLETED,
            endTimestamp = now,
            currentTimestamp = now,
            isMoving = false,
            breakdown = finalBreakdown
        )
        _tripState.value = completedState

        serviceScope.launch(Dispatchers.IO) {
            val entity = TripEntity.fromTripState(completedState)
            TaxiMeterApplication.instance.database.tripDao().updateTrip(entity)
        }

        releaseWakeLock()
        activeAssignmentId?.let { assignmentId ->
            serviceScope.launch(Dispatchers.IO) {
                tripAssignmentRepository.markCompleted(assignmentId)
                    .onFailure { android.util.Log.w("TripAssignment", "Unable to mark trip COMPLETED", it) }
            }
        }
        activeAssignmentId = null
        FloatingOverlayManager.hideOverlay(this)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        _isServiceRunning.value = false
        stopSelf()
    }

    override fun onDestroy() {
        FloatingOverlayManager.hideOverlay(this)
        tickerJob?.cancel()
        serviceScope.cancel()
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        releaseWakeLock()
        _isServiceRunning.value = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START_TRIP = "com.example.action.START_TRIP"
        const val ACTION_START_LOADED_TRIP = "com.example.action.START_LOADED_TRIP"
        const val ACTION_RESUME_RECOVERED = "com.example.action.RESUME_RECOVERED"
        const val ACTION_ADD_EXTRA = "com.example.action.ADD_EXTRA"
        const val ACTION_REMOVE_EXTRA = "com.example.action.REMOVE_EXTRA"
        const val ACTION_END_TRIP = "com.example.action.END_TRIP"

        const val EXTRA_TRIP_ID = "trip_id"
        const val EXTRA_ASSIGNMENT_ID = "assignment_id"
        const val EXTRA_RIDE_MODE = "ride_mode"
        const val EXTRA_LABEL = "extra_label"
        const val EXTRA_AMOUNT = "extra_amount"
        const val EXTRA_ID = "extra_id"

        private val _tripState = MutableStateFlow(TripState())
        val tripState: StateFlow<TripState> = _tripState.asStateFlow()

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        fun formatDuration(seconds: Long): String {
            val hrs = seconds / 3600
            val mins = (seconds % 3600) / 60
            val secs = seconds % 60
            return String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
        }

        fun startTrip(context: Context) {
            val intent = Intent(context, TaxiMeterService::class.java).apply {
                action = ACTION_START_TRIP
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun startLoadedTrip(context: Context, assignmentId: String, rideMode: String) {
            val intent = Intent(context, TaxiMeterService::class.java).apply {
                action = ACTION_START_LOADED_TRIP
                putExtra(EXTRA_ASSIGNMENT_ID, assignmentId)
                putExtra(EXTRA_RIDE_MODE, rideMode)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun resumeTrip(context: Context, tripId: Long) {
            val intent = Intent(context, TaxiMeterService::class.java).apply {
                action = ACTION_RESUME_RECOVERED
                putExtra(EXTRA_TRIP_ID, tripId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun addExtra(context: Context, label: String, amount: Double) {
            val intent = Intent(context, TaxiMeterService::class.java).apply {
                action = ACTION_ADD_EXTRA
                putExtra(EXTRA_LABEL, label)
                putExtra(EXTRA_AMOUNT, amount)
            }
            context.startService(intent)
        }

        fun removeExtra(context: Context, extraId: String) {
            val intent = Intent(context, TaxiMeterService::class.java).apply {
                action = ACTION_REMOVE_EXTRA
                putExtra(EXTRA_ID, extraId)
            }
            context.startService(intent)
        }

        fun endTrip(context: Context) {
            val intent = Intent(context, TaxiMeterService::class.java).apply {
                action = ACTION_END_TRIP
            }
            context.startService(intent)
        }

        fun resetReadyState() {
            _tripState.value = TripState(status = TripStatus.READY)
        }
    }
}

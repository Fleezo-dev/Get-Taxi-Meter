package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.R
import com.example.data.RideMode
import com.example.data.LoadedTripAssignment
import com.example.model.TripStatus
import com.example.service.FloatingOverlayManager
import com.example.service.TaxiMeterService
import com.example.ui.components.CompactMeterDashboard
import com.example.ui.components.ManualExtraChargeDialog
import com.example.ui.theme.AppCardBorder
import com.example.ui.theme.BrandRed
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MeterViewModel
import com.example.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MeterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val recoveredTrip by viewModel.recoveredTrip.collectAsState()
    val todayTrips by viewModel.todayCompletedTrips.collectAsState()
    val currentTariff by viewModel.currentTariff.collectAsState()
    val tripState by viewModel.tripState.collectAsState()

    var showTodaySummaryDialog by remember { mutableStateOf(false) }
    var showMoreMenuDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showRecoveredTripDialog by remember { mutableStateOf(false) }
    var showLoadTripDialog by remember { mutableStateOf(false) }
    var loadedTrip by remember { mutableStateOf<LoadedTripAssignment?>(null) }
    var loadTripOtp by remember { mutableStateOf("") }
    var loadTripError by remember { mutableStateOf<String?>(null) }
    var loadingTrip by remember { mutableStateOf(false) }

    // Recovery is loaded asynchronously; show the LOAD UNFINISHED TRIP prompt
    // as soon as Room returns an unfinished trip after HomeScreen is composed.
    LaunchedEffect(recoveredTrip?.tripId) {
        if (recoveredTrip != null) showRecoveredTripDialog = true
    }

    LaunchedEffect(recoveredTrip?.tripId) {
        if (recoveredTrip != null) showRecoveredTripDialog = true
    }
    var showRideModeDialog by remember { mutableStateOf(false) }
    var logoTaps by remember { mutableStateOf(0) }

    // Manual Extra Charges Dialog States
    var showAirportDialog by remember { mutableStateOf(false) }
    var showTollDialog by remember { mutableStateOf(false) }
    var showParkingDialog by remember { mutableStateOf(false) }

    // Overlay State & Lifecycle synchronization
    var isOverlayActive by remember { mutableStateOf(FloatingOverlayManager.isOverlayEnabled(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isOverlayActive = FloatingOverlayManager.isOverlayEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val todayEarnings = todayTrips.sumOf { it.currentFare }
    val todayDistanceKm = todayTrips.sumOf { it.totalDistanceMeters } / 1000.0

    // Dynamic current Date & Time
    val currentDate = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date())
    val currentTime = SimpleDateFormat("hh:mm a", Locale.US).format(Date())

    // Check GPS provider status
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    val isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: true

    // Permission launcher for Location & Notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineLocationGranted || coarseLocationGranted) {
            if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            viewModel.startTrip(context)
        }
    }

    val startTripForSelectedMode = {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineLocationGranted || coarseLocationGranted) {
            if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            viewModel.startTrip(context)
        } else {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    val onStartTripClick = {
        showRideModeDialog = true
    }

    val isTripActive = tripState.status == TripStatus.ACTIVE || tripState.status == TripStatus.WAITING

    CompactMeterDashboard(
        isTripActive = isTripActive,
        onActionClick = {
            if (isTripActive) {
                viewModel.navigateTo(Screen.LIVE_METER)
            } else {
                onStartTripClick()
            }
        },
        gpsReady = isGpsEnabled,
        gpsAccuracyMeters = if (tripState.gpsAccuracyMeters > 0) tripState.gpsAccuracyMeters else 4f,
        isMoving = tripState.isMoving,
        currentSpeedKmH = tripState.currentSpeedKmH,
        currentFare = if (isTripActive) tripState.breakdown.totalFare else currentTariff.baseFare,
        baseFare = currentTariff.baseFare,
        distanceFare = if (isTripActive) tripState.breakdown.distanceFare else 0.0,
        waitingFare = if (isTripActive) tripState.breakdown.waitingFare else 0.0,
        extraFare = if (isTripActive) tripState.breakdown.extraChargesTotal else 0.0,
        distanceKm = if (isTripActive) tripState.distanceKm else 0.0,
        ratePerKm = currentTariff.distanceRatePerKm,
        tripDurationFormatted = if (isTripActive) TaxiMeterService.formatDuration(tripState.tripDurationSeconds) else "00:00:00",
        waitingDurationFormatted = if (isTripActive) TaxiMeterService.formatDuration(tripState.waitingDurationSeconds) else "00:00:00",
        waitingRatePerMinute = currentTariff.waitingRatePerMinute,
        totalExtras = if (isTripActive) tripState.breakdown.extraChargesTotal else 0.0,
        dateText = currentDate,
        timeText = currentTime,
        onAddToll = {
            if (isTripActive) showTollDialog = true else onStartTripClick()
        },
        onAddParking = {
            if (isTripActive) showParkingDialog = true else onStartTripClick()
        },
        onAddAirport = {
            if (isTripActive) showAirportDialog = true else onStartTripClick()
        },
        onAddCustom = {
            if (isTripActive) viewModel.navigateTo(Screen.LIVE_METER) else onStartTripClick()
        },
        onMenuClick = { showMoreMenuDialog = true },
        onSettingsClick = { viewModel.navigateTo(Screen.SETTINGS) },
        onShareClick = { shareApp(context) },
        onTripHistoryClick = { viewModel.navigateTo(Screen.HISTORY) },
        onTariffSettingsClick = { viewModel.navigateTo(Screen.SETTINGS) },
        onTodaySummaryClick = { showTodaySummaryDialog = true },
        onMoreClick = { showMoreMenuDialog = true },
        onHiddenAdminClick = { viewModel.navigateTo(Screen.ADMIN) },
        isOverlayEnabled = isOverlayActive,
        onToggleOverlay = { enabled ->
            if (enabled) {
                if (!FloatingOverlayManager.canDrawOverlay(context)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    }
                } else {
                    FloatingOverlayManager.setOverlayEnabled(context, true)
                    isOverlayActive = true
                }
            } else {
                FloatingOverlayManager.setOverlayEnabled(context, false)
                isOverlayActive = false
            }
        },
        modifier = modifier
    )

    // RIDE MODE SELECTION
    if (showRideModeDialog) {
        AlertDialog(
            onDismissRequest = { showRideModeDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Select Ride Type",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose the tariff for this trip before starting the meter.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    listOf(
                        RideMode.CITY_RIDE to "City Ride",
                        RideMode.HOURLY_RENTAL to "Hourly Rental",
                        RideMode.OUTSTATION to "Outstation"
                    ).forEach { (mode, label) ->
                        OutlinedButton(
                            onClick = {
                                viewModel.setRideMode(mode)
                                showRideModeDialog = false
                                startTripForSelectedMode()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                contentColor = if (viewModel.selectedRideMode.value == mode) BrandRed else Color.Black
                            )
                        ) {
                            Text(label, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // MANUAL CHARGES DIALOGS
    if (showAirportDialog) {
        ManualExtraChargeDialog(
            title = "Airport Charges",
            onDismiss = { showAirportDialog = false },
            onAdd = { amount ->
                viewModel.addExtra(context, "Airport", amount)
            }
        )
    }

    if (showTollDialog) {
        ManualExtraChargeDialog(
            title = "Toll Charges",
            onDismiss = { showTollDialog = false },
            onAdd = { amount ->
                viewModel.addExtra(context, "Toll", amount)
            }
        )
    }

    if (showParkingDialog) {
        ManualExtraChargeDialog(
            title = "Parking Charges",
            onDismiss = { showParkingDialog = false },
            onAdd = { amount ->
                viewModel.addExtra(context, "Parking", amount)
            }
        )
    }

    // MANUAL LOADED TRIP DIALOG - this is separate from unfinished-trip recovery.
    if (showLoadTripDialog) {
        AlertDialog(
            onDismissRequest = { if (!loadingTrip) showLoadTripDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = if (loadedTrip == null) "LOAD TRIP",
                    else "TRIP READY",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (loadedTrip == null) {
                        Text("Enter the one-time OTP supplied by the dispatcher/admin.", color = TextSecondary, fontSize = 12.sp)
                        OutlinedTextField(
                            value = loadTripOtp,
                            onValueChange = {
                                if (it.all(Char::isDigit) && it.length <= 6) {
                                    loadTripOtp = it
                                    loadTripError = null
                                }
                            },
                            label = { Text("6-Digit Load OTP") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandRed,
                                unfocusedBorderColor = AppCardBorder
                            )
                        )
                        if (loadTripError != null) {
                            Text(loadTripError!!, color = BrandRed, fontSize = 12.sp)
                        }
                    } else {
                        val trip = loadedTrip!!
                        Text("Trip: " + trip.tripReference, color = BrandRed, fontWeight = FontWeight.Bold)
                        if (trip.customerName.isNotBlank()) Text("Customer: " + trip.customerName, color = TextSecondary, fontSize = 12.sp)
                        Text("Pickup: " + trip.pickup, color = Color.Black, fontSize = 13.sp)
                        Text("Drop: " + trip.drop, color = Color.Black, fontSize = 13.sp)
                        Text("Ride Type: " + trip.rideMode.replace('_', ' '), color = TextSecondary, fontSize = 12.sp)
                        Text("The trip is loaded to this phone. Tap START LOADED TRIP when you are ready.", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                if (loadedTrip == null) {
                    Button(
                        onClick = {
                            if (loadTripOtp.length != 6) {
                                loadTripError = "Enter the 6-digit OTP"
                            } else {
                                loadingTrip = true
                                loadTripError = null
                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                    viewModel.loadTripByOtp(loadTripOtp)
                                        .onSuccess {
                                            if (it == null) {
                                                loadTripError = "Invalid or already used trip OTP"
                                            } else {
                                                loadedTrip = it
                                            }
                                        }
                                        .onFailure { loadTripError = it.message ?: "Unable to load trip" }
                                    loadingTrip = false
                                }
                            }
                        },
                        enabled = !loadingTrip,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                    ) { Text(if (loadingTrip) "LOADING..." else "LOAD TRIP") }
                } else {
                    Button(
                        onClick = {
                            val trip = loadedTrip!!
                            viewModel.setRideMode(
                                try { RideMode.valueOf(trip.rideMode) } catch (_: Exception) { RideMode.CITY_RIDE }
                            )
                            loadedTrip = null
                            loadTripOtp = ""
                            showLoadTripDialog = false
                            startTripForSelectedMode()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                    ) { Text("START LOADED TRIP") }
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showLoadTripDialog = false },
                    enabled = !loadingTrip
                ) { Text("Cancel") }
            }
        )
    }

    // RECOVERED UNFINISHED TRIP DIALOG
    if (recoveredTrip != null && showRecoveredTripDialog) {
        val trip = recoveredTrip!!
        AlertDialog(
            onDismissRequest = { showRecoveredTripDialog = false },
            containerColor = Color.White,
            icon = {
                Icon(Icons.Default.Warning, contentDescription = null, tint = BrandRed, modifier = Modifier.size(32.dp))
            },
            title = {
                Text(
                    text = "LOAD UNFINISHED TRIP?",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "An active trip was in progress before the app closed. Fare accumulated: ₹${String.format(Locale.US, "%.2f", trip.currentFare)} (${String.format(Locale.US, "%.2f km", trip.totalDistanceMeters / 1000.0)}).",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRecoveredTripDialog = false
                        viewModel.resumeRecoveredTrip(context, trip.tripId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed, contentColor = Color.White)
                ) {
                    Text("Resume Trip", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showRecoveredTripDialog = false
                        viewModel.discardRecoveredTrip(trip)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Text("Discard")
                }
            }
        )
    }


    // ABOUT & CREDITS
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = "About Get Taxi Meter",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Get Taxi Kovai®",
                        color = BrandRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "App created by Basheer",
                        color = Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Habib Ad Management Service, Coimbatore",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Contact: 9043743777",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // TODAY'S SUMMARY DIALOG
    if (showTodaySummaryDialog) {
        AlertDialog(
            onDismissRequest = { showTodaySummaryDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Today's Summary",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Trips Today", color = TextSecondary, fontSize = 13.sp)
                        Text("${todayTrips.size}", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Today's Earnings", color = TextSecondary, fontSize = 13.sp)
                        Text(String.format(Locale.US, "₹%.2f", todayEarnings), color = BrandRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Distance Covered", color = TextSecondary, fontSize = 13.sp)
                        Text(String.format(Locale.US, "%.2f km", todayDistanceKm), color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTodaySummaryDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed, contentColor = Color.White)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // HAMBURGER / MORE OPTIONS MENU DIALOG
    if (showMoreMenuDialog) {
        AlertDialog(
            onDismissRequest = { showMoreMenuDialog = false },
            containerColor = Color.White,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_get_taxi_meter_brand),
                        contentDescription = "Get Taxi Meter",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Get", color = Color.Black, fontSize = 17.sp, fontWeight = FontWeight.Black)
                            Text(text = "Taxi", color = BrandRed, fontSize = 17.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Meter", color = Color(0xFF1E293B), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "Main Menu & Navigation",
                            color = TextSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 1. Driver Profile
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .clickable {
                                showMoreMenuDialog = false
                                viewModel.navigateTo(Screen.DRIVER_PROFILE)
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BrandRed.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = BrandRed, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Driver Profile & Vehicle", color = Color(0xFF0F172A), fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            Text("Driver name, phone & vehicle number", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    // 2. Setup Checklist
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .clickable {
                                showMoreMenuDialog = false
                                viewModel.navigateTo(Screen.SETUP_CHECKLIST)
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF16A34A).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Setup Checklist", color = Color(0xFF0F172A), fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            Text("Verify GPS, overlay & permissions", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    // 3. Device Activation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .clickable {
                                showMoreMenuDialog = false
                                viewModel.navigateTo(Screen.ACTIVATION)
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BrandRed.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = BrandRed, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Device Activation", color = Color(0xFF0F172A), fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            Text("Enter administrator activation code", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    // 4. Load Trip by OTP
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .clickable {
                                showMoreMenuDialog = false
                                loadTripOtp = ""
                                loadTripError = null
                                loadedTrip = null
                                showLoadTripDialog = true
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BrandRed.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = BrandRed, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Load Trip", color = Color(0xFF0F172A), fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            Text("Enter dispatcher OTP to receive an assigned trip", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    // 5. Trip History & Receipts
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .clickable {
                                showMoreMenuDialog = false
                                viewModel.navigateTo(Screen.HISTORY)
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BrandRed.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = BrandRed, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Trip History & Receipts", color = Color(0xFF0F172A), fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            Text("View past trips, fares & print receipts", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    // 5. Tariff & Meter Settings
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .clickable {
                                showMoreMenuDialog = false
                                viewModel.navigateTo(Screen.SETTINGS)
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B).copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF1E293B), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Tariff & Meter Settings", color = Color(0xFF0F172A), fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            Text("Base fare, km rate & waiting charges", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    // 6. Today's Summary
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .clickable {
                                showMoreMenuDialog = false
                                showTodaySummaryDialog = true
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BrandRed.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null, tint = BrandRed, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Today's Summary", color = Color(0xFF0F172A), fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            Text("${todayTrips.size} trips • ₹${String.format(Locale.US, "%.2f", todayEarnings)} earned", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    // 7. Battery & Background Settings
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .clickable {
                                showMoreMenuDialog = false
                                viewModel.navigateTo(Screen.BATTERY_GUIDANCE)
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF16A34A).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Battery & Background Settings", color = Color(0xFF0F172A), fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            Text("Keep meter active with screen off", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    // 8. Admin Panel (Protected by PIN 1981)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .clickable {
                                showMoreMenuDialog = false
                                viewModel.navigateTo(Screen.ADMIN)
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B).copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFF1E293B), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Admin Panel", color = Color(0xFF0F172A), fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            Text("PIN protected admin controls & code generator", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    // 9. About & Credits
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .clickable {
                                showMoreMenuDialog = false
                                showAboutDialog = true
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BrandRed.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = BrandRed, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("About & Credits", color = Color(0xFF0F172A), fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            Text("Get Taxi Kovai® • App created by Basheer", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    // 10. Share App
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .clickable {
                                showMoreMenuDialog = false
                                shareApp(context)
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2563EB).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Share Get Taxi Meter", color = Color(0xFF0F172A), fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            Text("Share app with fellow drivers", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showMoreMenuDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

private fun shareApp(context: Context) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            "Get Taxi Meter - Safe Rides, Transparent Fares. Professional GPS Taxi Meter for Android."
        )
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Get Taxi Meter")
    context.startActivity(shareIntent)
}

package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.data.AppDatabase
import com.example.data.TariffRepository
import com.example.data.RideModeRepository
import com.google.android.libraries.places.api.Places

class TaxiMeterApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var tariffRepository: TariffRepository
        private set

    lateinit var rideModeRepository: RideModeRepository
        private set

    lateinit var driverProfileRepository: com.example.data.DriverProfileRepository
        private set

    lateinit var activationRepository: com.example.data.ActivationRepository
        private set

    lateinit var driverPaymentRepository: com.example.data.DriverPaymentRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        tariffRepository = TariffRepository(this)
        rideModeRepository = RideModeRepository(this)
        driverProfileRepository = com.example.data.DriverProfileRepository(this)
        activationRepository = com.example.data.ActivationRepository(this)
        driverPaymentRepository = com.example.data.DriverPaymentRepository(this)
        initializePlaces()
        createNotificationChannel()
    }

    private fun initializePlaces() {
        val apiKey = BuildConfig.GOOGLE_MAPS_API_KEY.trim()
        if (apiKey.isBlank() || apiKey == "YOUR_GOOGLE_MAPS_API_KEY") {
            placesInitializationError = "Google Places API key is missing from the APK BuildConfig"
            android.util.Log.e("GooglePlaces", placesInitializationError!!)
            return
        }

        runCatching {
            if (!Places.isInitialized()) {
                Places.initializeWithNewPlacesApiEnabled(this, apiKey)
            }
            placesInitializationError = null
        }.onFailure {
            placesInitializationError = "${it.javaClass.simpleName}: ${it.message ?: "unknown initialization error"}"
            android.util.Log.e("GooglePlaces", placesInitializationError!!, it)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Active Taxi Meter",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live ongoing taxi fare, distance, and trip duration"
                setShowBadge(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    companion object {
        @Volatile
        var placesInitializationError: String? = null
            private set

        const val NOTIFICATION_CHANNEL_ID = "taxi_meter_channel"
        const val NOTIFICATION_ID = 1001

        lateinit var instance: TaxiMeterApplication
            private set
    }
}

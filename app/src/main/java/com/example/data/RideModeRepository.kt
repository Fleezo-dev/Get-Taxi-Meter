package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class RideMode {
    CITY_RIDE,
    HOURLY_RENTAL,
    OUTSTATION
}

data class RidePricing(
    val hourlyRate: Double = 350.0,
    val hourlyFreeKm: Double = 10.0,
    val hourlyExtraKmRate: Double = 20.0,
    val outstationDriverBata: Double = 300.0,
    val outstationPerKmRate: Double = 16.0
)

class RideModeRepository(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(
        "gtm_ride_mode_settings",
        Context.MODE_PRIVATE
    )

    private val _selectedMode = MutableStateFlow(loadMode())
    val selectedMode: StateFlow<RideMode> = _selectedMode.asStateFlow()

    private val _pricing = MutableStateFlow(loadPricing())
    val pricing: StateFlow<RidePricing> = _pricing.asStateFlow()

    fun setMode(mode: RideMode) {
        prefs.edit().putString("selected_mode", mode.name).apply()
        _selectedMode.value = mode
    }

    fun savePricing(value: RidePricing) {
        prefs.edit()
            .putFloat("hourly_rate", value.hourlyRate.toFloat())
            .putFloat("hourly_free_km", value.hourlyFreeKm.toFloat())
            .putFloat("hourly_extra_km_rate", value.hourlyExtraKmRate.toFloat())
            .putFloat("outstation_driver_bata", value.outstationDriverBata.toFloat())
            .putFloat("outstation_per_km_rate", value.outstationPerKmRate.toFloat())
            .apply()
        _pricing.value = value
    }

    private fun loadMode(): RideMode {
        return try {
            RideMode.valueOf(
                prefs.getString("selected_mode", RideMode.CITY_RIDE.name)
                    ?: RideMode.CITY_RIDE.name
            )
        } catch (_: Exception) {
            RideMode.CITY_RIDE
        }
    }

    private fun loadPricing(): RidePricing {
        return RidePricing(
            hourlyRate = prefs.getFloat("hourly_rate", 350f).toDouble(),
            hourlyFreeKm = prefs.getFloat("hourly_free_km", 10f).toDouble(),
            hourlyExtraKmRate = prefs.getFloat("hourly_extra_km_rate", 20f).toDouble(),
            outstationDriverBata = prefs.getFloat("outstation_driver_bata", 300f).toDouble(),
            outstationPerKmRate = prefs.getFloat("outstation_per_km_rate", 16f).toDouble()
        )
    }
}

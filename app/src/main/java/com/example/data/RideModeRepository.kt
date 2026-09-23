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
    val hourlyRate: Double = DEFAULT_HOURLY_RATE,
    val hourlyFreeKm: Double = DEFAULT_HOURLY_FREE_KM,
    val hourlyExtraKmRate: Double = DEFAULT_HOURLY_EXTRA_KM_RATE,
    val outstationDriverBata: Double = DEFAULT_OUTSTATION_DRIVER_BATA,
    val outstationPerKmRate: Double = DEFAULT_OUTSTATION_PER_KM_RATE
) {
    companion object {
        const val DEFAULT_HOURLY_RATE = 350.0
        const val DEFAULT_HOURLY_FREE_KM = 10.0
        const val DEFAULT_HOURLY_EXTRA_KM_RATE = 20.0
        const val DEFAULT_OUTSTATION_DRIVER_BATA = 500.0
        const val DEFAULT_OUTSTATION_PER_KM_RATE = 30.0
    }
}

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
            hourlyRate = prefs.getFloat("hourly_rate", RidePricing.DEFAULT_HOURLY_RATE.toFloat()).toDouble(),
            hourlyFreeKm = prefs.getFloat("hourly_free_km", RidePricing.DEFAULT_HOURLY_FREE_KM.toFloat()).toDouble(),
            hourlyExtraKmRate = prefs.getFloat("hourly_extra_km_rate", RidePricing.DEFAULT_HOURLY_EXTRA_KM_RATE.toFloat()).toDouble(),
            outstationDriverBata = prefs.getFloat("outstation_driver_bata", RidePricing.DEFAULT_OUTSTATION_DRIVER_BATA.toFloat()).toDouble(),
            outstationPerKmRate = prefs.getFloat("outstation_per_km_rate", RidePricing.DEFAULT_OUTSTATION_PER_KM_RATE.toFloat()).toDouble()
        )
    }
}

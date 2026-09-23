package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.Tariff
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TariffRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("taxi_meter_tariffs", Context.MODE_PRIVATE)

    private val _currentTariff = MutableStateFlow(loadTariff())
    val currentTariff: StateFlow<Tariff> = _currentTariff.asStateFlow()

    fun loadTariff(): Tariff {
        val id = prefs.getString("tariff_id", "standard_day") ?: "standard_day"
        val name = prefs.getString("tariff_name", "Standard City Tariff") ?: "Standard City Tariff"
        val baseFare = prefs.getFloat("base_fare", 80.0f).toDouble()
        val minFare = prefs.getFloat("min_fare", 0.0f).toDouble()
        val distanceRate = prefs.getFloat("distance_rate", 28.0f).toDouble()
        val waitingRate = prefs.getFloat("waiting_rate", 2.0f).toDouble()
        val freeDistance = prefs.getFloat("free_distance", 0.0f).toDouble()
        val freeWaiting = 0.0
        val speedThreshold = prefs.getFloat("speed_threshold", 5.0f).toDouble()
        val distanceChargeEnabled = prefs.getBoolean("distance_charge_enabled", true)
        val nightSurcharge = prefs.getFloat("night_surcharge", 1.0f).toDouble()

        return Tariff(
            id = id,
            name = name,
            baseFare = baseFare,
            minimumFare = minFare,
            distanceRatePerKm = distanceRate,
            waitingRatePerMinute = waitingRate,
            freeDistanceKm = freeDistance,
            freeWaitingMinutes = freeWaiting,
            waitingSpeedThresholdKmH = speedThreshold,
            distanceChargeEnabled = distanceChargeEnabled,
            nightSurchargeMultiplier = nightSurcharge
        )
    }

    fun saveTariff(tariff: Tariff) {
        prefs.edit().apply {
            putString("tariff_id", tariff.id)
            putString("tariff_name", tariff.name)
            putFloat("base_fare", tariff.baseFare.toFloat())
            putFloat("min_fare", 0.0f)
            putFloat("distance_rate", tariff.distanceRatePerKm.toFloat())
            putFloat("waiting_rate", tariff.waitingRatePerMinute.toFloat())
            putFloat("free_distance", tariff.freeDistanceKm.toFloat())
            putFloat("free_waiting", 0.0f)
            putBoolean("distance_charge_enabled", tariff.distanceChargeEnabled)
            putFloat("speed_threshold", tariff.waitingSpeedThresholdKmH.toFloat())
            putFloat("night_surcharge", tariff.nightSurchargeMultiplier.toFloat())
            apply()
        }
        _currentTariff.value = tariff
    }
}

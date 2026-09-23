package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.ExtraCharge
import com.example.model.FareRounding
import com.example.model.MeterBreakdown
import com.example.model.Tariff
import com.example.model.TripState
import com.example.model.TripStatus

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true)
    val tripId: Long = 0L,
    val status: String,
    val startTimestamp: Long,
    val currentTimestamp: Long,
    val endTimestamp: Long? = null,
    val startLatitude: Double? = null,
    val startLongitude: Double? = null,
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    val totalDistanceMeters: Double,
    val movingDistanceMeters: Double,
    val waitingDurationSeconds: Long,
    val tripDurationSeconds: Long,
    val currentFare: Double,
    val baseFare: Double,
    val distanceFare: Double,
    val waitingFare: Double,
    val extraChargesTotal: Double,
    val extraChargesJson: String = "",
    val tariffId: String,
    val tariffName: String,
    val tariffBaseFare: Double,
    val tariffMinFare: Double,
    val tariffDistanceRate: Double,
    val tariffDistanceChargeEnabled: Boolean = true,
    val tariffWaitingRate: Double,
    val tariffFreeDistanceKm: Double,
    val tariffFreeWaitingMin: Double,
    val tariffRounding: String,
    val lastGpsTimestamp: Long,
    val isRecovered: Boolean = false
) {
    fun toTripState(): TripState {
        val parsedTariff = Tariff(
            id = tariffId,
            name = tariffName,
            baseFare = tariffBaseFare,
            minimumFare = tariffMinFare,
            distanceRatePerKm = tariffDistanceRate,
            distanceChargeEnabled = tariffDistanceChargeEnabled,
            waitingRatePerMinute = tariffWaitingRate,
            freeDistanceKm = tariffFreeDistanceKm,
            freeWaitingMinutes = tariffFreeWaitingMin,
            fareRounding = try {
                FareRounding.valueOf(tariffRounding)
            } catch (e: Exception) {
                FareRounding.NEAREST_ONE
            }
        )

        val extrasList = parseExtras(extraChargesJson)
        val breakdown = MeterBreakdown(
            baseFare = baseFare,
            totalDistanceKm = totalDistanceMeters / 1000.0,
            chargeableDistanceKm = kotlin.math.max(0.0, (totalDistanceMeters / 1000.0) - tariffFreeDistanceKm),
            distanceFare = distanceFare,
            totalWaitingMinutes = waitingDurationSeconds / 60.0,
            chargeableWaitingMinutes = kotlin.math.max(0.0, (waitingDurationSeconds / 60.0) - tariffFreeWaitingMin),
            waitingFare = waitingFare,
            subtotalBeforeMin = baseFare + distanceFare + waitingFare,
            minFareApplied = currentFare == tariffMinFare && (baseFare + distanceFare + waitingFare) < tariffMinFare,
            meterFare = currentFare - extraChargesTotal,
            extraChargesTotal = extraChargesTotal,
            totalFare = currentFare
        )

        return TripState(
            tripId = tripId,
            status = try { TripStatus.valueOf(status) } catch (e: Exception) { TripStatus.ACTIVE },
            startTimestamp = startTimestamp,
            currentTimestamp = currentTimestamp,
            endTimestamp = endTimestamp,
            totalDistanceMeters = totalDistanceMeters,
            movingDistanceMeters = movingDistanceMeters,
            waitingDurationSeconds = waitingDurationSeconds,
            tripDurationSeconds = tripDurationSeconds,
            currentSpeedKmH = 0f,
            isMoving = false,
            gpsAccuracyMeters = 0f,
            gpsStatus = "Recovered from storage",
            lastGpsTimestamp = lastGpsTimestamp,
            startLatitude = startLatitude,
            startLongitude = startLongitude,
            currentLatitude = currentLatitude,
            currentLongitude = currentLongitude,
            breakdown = breakdown,
            extras = extrasList,
            tariff = parsedTariff,
            isRecovered = isRecovered
        )
    }

    companion object {
        fun fromTripState(state: TripState): TripEntity {
            return TripEntity(
                tripId = state.tripId,
                status = state.status.name,
                startTimestamp = state.startTimestamp,
                currentTimestamp = state.currentTimestamp,
                endTimestamp = state.endTimestamp,
                startLatitude = state.startLatitude,
                startLongitude = state.startLongitude,
                currentLatitude = state.currentLatitude,
                currentLongitude = state.currentLongitude,
                totalDistanceMeters = state.totalDistanceMeters,
                movingDistanceMeters = state.movingDistanceMeters,
                waitingDurationSeconds = state.waitingDurationSeconds,
                tripDurationSeconds = state.tripDurationSeconds,
                currentFare = state.breakdown.totalFare,
                baseFare = state.breakdown.baseFare,
                distanceFare = state.breakdown.distanceFare,
                waitingFare = state.breakdown.waitingFare,
                extraChargesTotal = state.breakdown.extraChargesTotal,
                extraChargesJson = serializeExtras(state.extras),
                tariffId = state.tariff.id,
                tariffName = state.tariff.name,
                tariffBaseFare = state.tariff.baseFare,
                tariffMinFare = state.tariff.minimumFare,
                tariffDistanceRate = state.tariff.distanceRatePerKm,
                tariffDistanceChargeEnabled = state.tariff.distanceChargeEnabled,
                tariffWaitingRate = state.tariff.waitingRatePerMinute,
                tariffFreeDistanceKm = state.tariff.freeDistanceKm,
                tariffFreeWaitingMin = state.tariff.freeWaitingMinutes,
                tariffRounding = state.tariff.fareRounding.name,
                lastGpsTimestamp = state.lastGpsTimestamp,
                isRecovered = state.isRecovered
            )
        }

        fun serializeExtras(extras: List<ExtraCharge>): String {
            if (extras.isEmpty()) return ""
            return extras.joinToString(";") { "${it.id}|${it.label}|${it.amount}" }
        }

        fun parseExtras(raw: String): List<ExtraCharge> {
            if (raw.isBlank()) return emptyList()
            return raw.split(";").mapNotNull { entry ->
                val parts = entry.split("|")
                if (parts.size == 3) {
                    val amount = parts[2].toDoubleOrNull() ?: 0.0
                    ExtraCharge(parts[0], parts[1], amount)
                } else null
            }
        }
    }
}

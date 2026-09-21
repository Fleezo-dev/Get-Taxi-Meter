package com.example.engine

import com.example.model.ExtraCharge
import com.example.model.FareRounding
import com.example.model.MeterBreakdown
import com.example.model.Tariff
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

object MeterEngine {

    /**
     * Calculates the deterministic fare based on distance, waiting time, extra charges, and tariff.
     */
    fun calculateFare(
        distanceMeters: Double,
        waitingDurationSeconds: Long,
        extras: List<ExtraCharge>,
        tariff: Tariff
    ): MeterBreakdown {
        val totalDistanceKm = distanceMeters / 1000.0
        val chargeableDistanceKm = max(0.0, totalDistanceKm - tariff.freeDistanceKm)
        val rawDistanceFare = chargeableDistanceKm * tariff.distanceRatePerKm

        val totalWaitingMinutes = waitingDurationSeconds / 60.0
        val chargeableWaitingMinutes = max(0.0, totalWaitingMinutes - tariff.freeWaitingMinutes)
        val rawWaitingFare = chargeableWaitingMinutes * tariff.waitingRatePerMinute

        val subtotal = (tariff.baseFare + rawDistanceFare + rawWaitingFare) * tariff.nightSurchargeMultiplier
        val minFareApplied = subtotal < tariff.minimumFare
        val meterFareBeforeRounding = max(subtotal, tariff.minimumFare)

        val roundedMeterFare = applyRounding(meterFareBeforeRounding, tariff.fareRounding)

        val extraChargesTotal = extras.sumOf { it.amount }
        val finalTotalFare = roundedMeterFare + extraChargesTotal

        return MeterBreakdown(
            baseFare = tariff.baseFare,
            totalDistanceKm = totalDistanceKm,
            chargeableDistanceKm = chargeableDistanceKm,
            distanceFare = rawDistanceFare,
            totalWaitingMinutes = totalWaitingMinutes,
            chargeableWaitingMinutes = chargeableWaitingMinutes,
            waitingFare = rawWaitingFare,
            subtotalBeforeMin = subtotal,
            minFareApplied = minFareApplied,
            meterFare = roundedMeterFare,
            extraChargesTotal = extraChargesTotal,
            totalFare = finalTotalFare
        )
    }

    /**
     * Applies the configured rounding rule to the meter fare.
     */
    fun applyRounding(amount: Double, rounding: FareRounding): Double {
        return when (rounding) {
            FareRounding.EXACT -> round(amount * 100.0) / 100.0
            FareRounding.NEAREST_ONE -> round(amount)
            FareRounding.NEAREST_FIVE -> round(amount / 5.0) * 5.0
            FareRounding.CEIL_ONE -> ceil(amount)
        }
    }

    /**
     * Computes the great-circle distance between two GPS coordinates using the Haversine formula in meters.
     */
    fun computeHaversineDistanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    data class FilterResult(
        val isAccepted: Boolean,
        val distanceDeltaMeters: Double,
        val reason: String
    )

    /**
     * Real-world GPS filter to reject noise, inaccurate fixes, and teleportation jumps.
     */
    fun filterLocation(
        newLat: Double,
        newLon: Double,
        newAccuracy: Float,
        newTimeMs: Long,
        lastLat: Double?,
        lastLon: Double?,
        lastTimeMs: Long?,
        maxAccuracyAllowedMeters: Float = 35f,
        maxSpeedAllowedMetersPerSec: Double = 50.0 // ~180 km/h
    ): FilterResult {
        // Reject inaccurate fixes
        if (newAccuracy > maxAccuracyAllowedMeters) {
            return FilterResult(false, 0.0, "Accuracy too low: ${newAccuracy}m > ${maxAccuracyAllowedMeters}m")
        }

        // First point
        if (lastLat == null || lastLon == null || lastTimeMs == null) {
            return FilterResult(true, 0.0, "Initial GPS fix accepted")
        }

        val timeDeltaMs = newTimeMs - lastTimeMs
        if (timeDeltaMs <= 0) {
            return FilterResult(false, 0.0, "Duplicate or backwards timestamp")
        }

        val distance = computeHaversineDistanceMeters(lastLat, lastLon, newLat, newLon)

        // Prevent micro-jitter when stopped (GPS noise within 2.5 meters)
        if (distance < 2.5) {
            return FilterResult(false, 0.0, "GPS micro-jitter ignored (< 2.5m)")
        }

        // Check for unrealistic speed jump
        val timeDeltaSeconds = timeDeltaMs / 1000.0
        val impliedSpeedMps = distance / timeDeltaSeconds
        if (impliedSpeedMps > maxSpeedAllowedMetersPerSec) {
            return FilterResult(false, 0.0, "Unrealistic speed jump: ${impliedSpeedMps.toInt()} m/s")
        }

        return FilterResult(true, distance, "Accepted valid movement: +${String.format("%.1f", distance)}m")
    }

    /**
     * Hysteresis state evaluator for moving vs waiting.
     * Prevents rapidly switching state when crawling in traffic or stopping at signals.
     */
    data class MotionState(
        val isMoving: Boolean,
        val consecutiveStopTicks: Int,
        val consecutiveMoveTicks: Int
    )

    fun evaluateMotion(
        currentSpeedKmH: Float,
        thresholdKmH: Float,
        currentState: MotionState,
        requiredStopTicks: Int = 3,
        requiredMoveTicks: Int = 2
    ): MotionState {
        return if (currentSpeedKmH < thresholdKmH) {
            val stopTicks = currentState.consecutiveStopTicks + 1
            if (stopTicks >= requiredStopTicks) {
                MotionState(isMoving = false, consecutiveStopTicks = stopTicks, consecutiveMoveTicks = 0)
            } else {
                currentState.copy(consecutiveStopTicks = stopTicks, consecutiveMoveTicks = 0)
            }
        } else {
            val moveTicks = currentState.consecutiveMoveTicks + 1
            if (moveTicks >= requiredMoveTicks) {
                MotionState(isMoving = true, consecutiveStopTicks = 0, consecutiveMoveTicks = moveTicks)
            } else {
                currentState.copy(consecutiveStopTicks = 0, consecutiveMoveTicks = moveTicks)
            }
        }
    }
}

package com.example

import com.example.engine.MeterEngine
import com.example.model.ExtraCharge
import com.example.model.FareRounding
import com.example.model.Tariff
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MeterEngineTest {

    private val standardTariff = Tariff(
        id = "test_tariff",
        name = "City Tariff",
        baseFare = 50.0,
        minimumFare = 50.0,
        distanceRatePerKm = 18.0,
        waitingRatePerMinute = 2.0,
        freeDistanceKm = 1.5,
        freeWaitingMinutes = 5.0,
        waitingSpeedThresholdKmH = 5.0,
        fareRounding = FareRounding.NEAREST_ONE
    )

    @Test
    fun testBaseFareWithinFreeAllowances() {
        // Within free distance (1.0 km <= 1.5 km) and within free waiting (3 mins <= 5 mins)
        val breakdown = MeterEngine.calculateFare(
            distanceMeters = 1000.0, // 1 km
            waitingDurationSeconds = 180L, // 3 minutes
            extras = emptyList(),
            tariff = standardTariff
        )

        assertEquals(50.0, breakdown.baseFare, 0.01)
        assertEquals(0.0, breakdown.distanceFare, 0.01)
        assertEquals(0.0, breakdown.waitingFare, 0.01)
        assertEquals(50.0, breakdown.meterFare, 0.01)
        assertEquals(50.0, breakdown.totalFare, 0.01)
    }

    @Test
    fun testDistanceFareExceedingFreeAllowance() {
        // 6.5 km total distance -> 5.0 km chargeable * 18.0 = ₹90.0
        // Total fare = Base ₹50 + Dist ₹90 = ₹140.0
        val breakdown = MeterEngine.calculateFare(
            distanceMeters = 6500.0,
            waitingDurationSeconds = 0L,
            extras = emptyList(),
            tariff = standardTariff
        )

        assertEquals(5.0, breakdown.chargeableDistanceKm, 0.01)
        assertEquals(90.0, breakdown.distanceFare, 0.01)
        assertEquals(140.0, breakdown.meterFare, 0.01)
        assertEquals(140.0, breakdown.totalFare, 0.01)
    }

    @Test
    fun testWaitingFareExceedingFreeAllowance() {
        // 15 minutes waiting -> 10 minutes chargeable * 2.0 = ₹20.0
        // Base ₹50 + Waiting ₹20 = ₹70.0
        val breakdown = MeterEngine.calculateFare(
            distanceMeters = 0.0,
            waitingDurationSeconds = 900L, // 15 mins
            extras = emptyList(),
            tariff = standardTariff
        )

        assertEquals(10.0, breakdown.chargeableWaitingMinutes, 0.01)
        assertEquals(20.0, breakdown.waitingFare, 0.01)
        assertEquals(70.0, breakdown.meterFare, 0.01)
        assertEquals(70.0, breakdown.totalFare, 0.01)
    }

    @Test
    fun testExtraChargesAddition() {
        val extras = listOf(
            ExtraCharge("toll-1", "Highway Toll", 60.0),
            ExtraCharge("park-1", "Airport Parking", 40.0)
        )

        val breakdown = MeterEngine.calculateFare(
            distanceMeters = 1500.0, // exactly free distance
            waitingDurationSeconds = 300L, // exactly free waiting
            extras = extras,
            tariff = standardTariff
        )

        assertEquals(50.0, breakdown.meterFare, 0.01)
        assertEquals(100.0, breakdown.extraChargesTotal, 0.01)
        assertEquals(150.0, breakdown.totalFare, 0.01)
    }

    @Test
    fun testMinimumFareEnforcement() {
        val highMinTariff = standardTariff.copy(baseFare = 30.0, minimumFare = 80.0)
        val breakdown = MeterEngine.calculateFare(
            distanceMeters = 500.0,
            waitingDurationSeconds = 0L,
            extras = emptyList(),
            tariff = highMinTariff
        )

        assertTrue(breakdown.minFareApplied)
        assertEquals(80.0, breakdown.meterFare, 0.01)
        assertEquals(80.0, breakdown.totalFare, 0.01)
    }

    @Test
    fun testRoundingModes() {
        assertEquals(42.34, MeterEngine.applyRounding(42.344, FareRounding.EXACT), 0.001)
        assertEquals(42.0, MeterEngine.applyRounding(42.4, FareRounding.NEAREST_ONE), 0.001)
        assertEquals(43.0, MeterEngine.applyRounding(42.6, FareRounding.NEAREST_ONE), 0.001)
        assertEquals(45.0, MeterEngine.applyRounding(43.0, FareRounding.NEAREST_FIVE), 0.001)
        assertEquals(43.0, MeterEngine.applyRounding(42.1, FareRounding.CEIL_ONE), 0.001)
    }

    @Test
    fun testGpsFilterRejectsLowAccuracy() {
        val result = MeterEngine.filterLocation(
            newLat = 12.9716,
            newLon = 77.5946,
            newAccuracy = 55f, // > 35m
            newTimeMs = 10000L,
            lastLat = 12.9710,
            lastLon = 77.5940,
            lastTimeMs = 9000L
        )

        assertFalse(result.isAccepted)
        assertTrue(result.reason.contains("Accuracy too low"))
    }

    @Test
    fun testGpsFilterRejectsTeleportationJump() {
        val result = MeterEngine.filterLocation(
            newLat = 13.5000, // ~60 km away!
            newLon = 77.5946,
            newAccuracy = 8f,
            newTimeMs = 2000L,
            lastLat = 12.9716,
            lastLon = 77.5946,
            lastTimeMs = 1000L // only 1 second later
        )

        assertFalse(result.isAccepted)
        assertTrue(result.reason.contains("Unrealistic speed jump"))
    }

    @Test
    fun testGpsFilterAcceptsValidDrivingDisplacement() {
        val result = MeterEngine.filterLocation(
            newLat = 12.97180,
            newLon = 77.59460,
            newAccuracy = 6f,
            newTimeMs = 2000L,
            lastLat = 12.97160,
            lastLon = 77.59460,
            lastTimeMs = 1000L
        )

        assertTrue(result.isAccepted)
        assertTrue(result.distanceDeltaMeters > 5.0)
    }

    @Test
    fun testMotionHysteresisMovingToWaiting() {
        var motion = MeterEngine.MotionState(isMoving = true, consecutiveStopTicks = 0, consecutiveMoveTicks = 5)

        // 1st slow tick: should not immediately switch to stopped
        motion = MeterEngine.evaluateMotion(2.0f, 5.0f, motion, requiredStopTicks = 3)
        assertTrue(motion.isMoving)

        // 2nd slow tick
        motion = MeterEngine.evaluateMotion(1.5f, 5.0f, motion, requiredStopTicks = 3)
        assertTrue(motion.isMoving)

        // 3rd consecutive slow tick: now transitions to stopped!
        motion = MeterEngine.evaluateMotion(0.0f, 5.0f, motion, requiredStopTicks = 3)
        assertFalse(motion.isMoving)
    }
}

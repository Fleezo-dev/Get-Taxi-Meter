package com.example.model

enum class TripStatus {
    READY,
    STARTING,
    ACTIVE,
    WAITING,
    ENDING,
    COMPLETED,
    CANCELLED
}

enum class FareRounding {
    EXACT,
    NEAREST_ONE,
    NEAREST_FIVE,
    CEIL_ONE
}

data class ExtraCharge(
    val id: String,
    val label: String,
    val amount: Double
)

data class Tariff(
    val id: String = "standard_day",
    val name: String = "Standard Tariff",
    val baseFare: Double = 50.0,
    val minimumFare: Double = 50.0,
    val distanceRatePerKm: Double = 18.0,
    val waitingRatePerMinute: Double = 2.0,
    val freeDistanceKm: Double = 1.5,
    val freeWaitingMinutes: Double = 5.0,
    val waitingSpeedThresholdKmH: Double = 5.0,
    val fareRounding: FareRounding = FareRounding.NEAREST_ONE,
    val nightSurchargeMultiplier: Double = 1.0 // 1.25 for 25% night surge
)

data class MeterBreakdown(
    val baseFare: Double = 50.0,
    val totalDistanceKm: Double = 0.0,
    val chargeableDistanceKm: Double = 0.0,
    val distanceFare: Double = 0.0,
    val totalWaitingMinutes: Double = 0.0,
    val chargeableWaitingMinutes: Double = 0.0,
    val waitingFare: Double = 0.0,
    val subtotalBeforeMin: Double = 50.0,
    val minFareApplied: Boolean = false,
    val meterFare: Double = 50.0,
    val extraChargesTotal: Double = 0.0,
    val totalFare: Double = 50.0
)

data class TripState(
    val tripId: Long = 0L,
    val status: TripStatus = TripStatus.READY,
    val startTimestamp: Long = 0L,
    val currentTimestamp: Long = 0L,
    val endTimestamp: Long? = null,
    val totalDistanceMeters: Double = 0.0,
    val movingDistanceMeters: Double = 0.0,
    val waitingDurationSeconds: Long = 0L,
    val tripDurationSeconds: Long = 0L,
    val currentSpeedKmH: Float = 0f,
    val isMoving: Boolean = false,
    val gpsAccuracyMeters: Float = 0f,
    val gpsStatus: String = "Ready",
    val lastGpsTimestamp: Long = 0L,
    val startLatitude: Double? = null,
    val startLongitude: Double? = null,
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    val breakdown: MeterBreakdown = MeterBreakdown(),
    val extras: List<ExtraCharge> = emptyList(),
    val tariff: Tariff = Tariff(),
    val isRecovered: Boolean = false
) {
    val distanceKm: Double get() = totalDistanceMeters / 1000.0
    val movingDistanceKm: Double get() = movingDistanceMeters / 1000.0
}

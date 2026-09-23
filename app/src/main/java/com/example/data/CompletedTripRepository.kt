package com.example.data

import com.example.model.TripState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Mirrors the final local meter result to Firestore.
 * Room remains the offline source of truth; Firebase sync is best-effort.
 */
class CompletedTripRepository {
    companion object { private const val COLLECTION = "completed_trips" }
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun upload(state: TripState, assignmentId: String? = null): Result<String> {
        return runCatching {
            val user = auth.currentUser ?: auth.signInAnonymously().await().user
            requireNotNull(user) { "Unable to authenticate meter device" }
            require(user.isAnonymous) { "Meter upload requires the device session" }

            val documentId = UUID.randomUUID().toString()
            val data = hashMapOf<String, Any?>(
                "tripId" to state.tripId,
                "ownerUid" to user.uid,
                "assignmentId" to assignmentId,
                "status" to "COMPLETED",
                "startTimestamp" to state.startTimestamp,
                "endTimestamp" to state.endTimestamp,
                "startLatitude" to state.startLatitude,
                "startLongitude" to state.startLongitude,
                "currentLatitude" to state.currentLatitude,
                "currentLongitude" to state.currentLongitude,
                "totalDistanceMeters" to state.totalDistanceMeters,
                "movingDistanceMeters" to state.movingDistanceMeters,
                "waitingDurationSeconds" to state.waitingDurationSeconds,
                "tripDurationSeconds" to state.tripDurationSeconds,
                "currentFare" to state.breakdown.totalFare,
                "baseFare" to state.breakdown.baseFare,
                "distanceFare" to state.breakdown.distanceFare,
                "waitingFare" to state.breakdown.waitingFare,
                "extraChargesTotal" to state.breakdown.extraChargesTotal,
                "extraCharges" to state.extras.map {
                    mapOf("id" to it.id, "label" to it.label, "amount" to it.amount)
                },
                "tariffId" to state.tariff.id,
                "tariffName" to state.tariff.name,
                "tariffBaseFare" to state.tariff.baseFare,
                "tariffMinFare" to state.tariff.minimumFare,
                "tariffDistanceRate" to state.tariff.distanceRatePerKm,
                "tariffWaitingRate" to state.tariff.waitingRatePerMinute,
                "tariffFreeDistanceKm" to state.tariff.freeDistanceKm,
                "tariffFreeWaitingMin" to state.tariff.freeWaitingMinutes,
                "uploadedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection(COLLECTION).document(documentId).set(data).await()
            documentId
        }
    }
}

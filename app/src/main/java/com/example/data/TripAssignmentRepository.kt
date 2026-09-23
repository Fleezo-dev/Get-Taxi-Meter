package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

data class LoadedTripAssignment(
    val assignmentId: String,
    val deviceId: String,
    val ownerUid: String,
    val tripReference: String,
    val customerName: String,
    val customerMobile: String,
    val pickup: String,
    val drop: String,
    val rideMode: String,
    val status: String
)

class TripAssignmentRepository {

    companion object {
        private const val COLLECTION = "trip_assignments"
        private const val STATUS_ASSIGNED = "ASSIGNED"
        private const val STATUS_CLAIMED = "CLAIMED"
        private const val OTP_LENGTH = 6

        private fun hashOtp(otp: String): String =
            MessageDigest.getInstance("SHA-256")
                .digest(otp.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
    }

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val secureRandom = SecureRandom()

    suspend fun createAssignment(
        deviceId: String,
        ownerUid: String,
        tripReference: String,
        customerName: String,
        customerMobile: String,
        pickup: String,
        drop: String,
        rideMode: String
    ): Result<Pair<LoadedTripAssignment, String>> {
        return runCatching {
            require(deviceId.isNotBlank()) { "Driver device is required" }
            require(ownerUid.isNotBlank()) { "Driver authentication owner is missing" }
            require(pickup.isNotBlank()) { "Pickup is required" }
            require(drop.isNotBlank()) { "Drop is required" }

            val otp = buildOtp()
            val ref = firestore.collection(COLLECTION).document(UUID.randomUUID().toString())
            val tripRef = tripReference.ifBlank { "TRIP-" + ref.id.take(6).uppercase() }
            val data = hashMapOf<String, Any>(
                "assignmentId" to ref.id,
                "deviceId" to deviceId,
                "ownerUid" to ownerUid,
                "tripReference" to tripRef,
                "customerName" to customerName,
                "customerMobile" to customerMobile,
                "pickup" to pickup,
                "drop" to drop,
                "rideMode" to rideMode,
                "otpHash" to hashOtp(otp),
                "status" to STATUS_ASSIGNED,
                "createdAt" to FieldValue.serverTimestamp()
            )
            ref.set(data).await()

            Pair(
                LoadedTripAssignment(
                    assignmentId = ref.id,
                    deviceId = deviceId,
                    ownerUid = ownerUid,
                    tripReference = tripRef,
                    customerName = customerName,
                    customerMobile = customerMobile,
                    pickup = pickup,
                    drop = drop,
                    rideMode = rideMode,
                    status = STATUS_ASSIGNED
                ),
                otp
            )
        }
    }

    suspend fun claimByOtp(otp: String): Result<LoadedTripAssignment?> {
        return runCatching {
            val user = auth.currentUser ?: auth.signInAnonymously().await().user
            requireNotNull(user) { "Unable to authenticate this device" }
            require(user.isAnonymous) { "Driver device authentication required" }

            val cleanOtp = otp.filter(Char::isDigit)
            if (cleanOtp.length != OTP_LENGTH) return@runCatching null

            val snapshot = firestore.collection(COLLECTION)
                .whereEqualTo("ownerUid", user.uid)
                .whereEqualTo("status", STATUS_ASSIGNED)
                .limit(10)
                .get()
                .await()

            val match = snapshot.documents.firstOrNull { doc ->
                doc.getString("otpHash") == hashOtp(cleanOtp)
            } ?: return@runCatching null

            val ref = match.reference
            firestore.runTransaction { transaction ->
                val latest = transaction.get(ref)
                if (!latest.exists() || latest.getString("status") != STATUS_ASSIGNED) {
                    throw IllegalStateException("This trip has already been loaded")
                }
                transaction.update(
                    ref,
                    mapOf(
                        "status" to STATUS_CLAIMED,
                        "claimedAt" to FieldValue.serverTimestamp(),
                        "claimedByUid" to user.uid
                    )
                )
                null
            }.await()

            LoadedTripAssignment(
                assignmentId = match.id,
                deviceId = match.getString("deviceId").orEmpty(),
                ownerUid = match.getString("ownerUid").orEmpty(),
                tripReference = match.getString("tripReference").orEmpty(),
                customerName = match.getString("customerName").orEmpty(),
                customerMobile = match.getString("customerMobile").orEmpty(),
                pickup = match.getString("pickup").orEmpty(),
                drop = match.getString("drop").orEmpty(),
                rideMode = match.getString("rideMode") ?: "CITY_RIDE",
                status = STATUS_CLAIMED
            )
        }
    }

    private fun buildOtp(): String =
        (100000 + secureRandom.nextInt(900000)).toString()
}

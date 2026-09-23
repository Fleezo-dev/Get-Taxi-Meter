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
            MessageDigest.getInstance("SHA-256").digest(otp.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
    }

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val secureRandom = SecureRandom()

    // Universal trip: the dispatcher creates it without selecting a driver.
    suspend fun createAssignment(
        tripReference: String,
        customerName: String,
        customerMobile: String,
        pickup: String,
        drop: String,
        rideMode: String
    ): Result<Pair<LoadedTripAssignment, String>> = runCatching {
        require(auth.currentUser?.isAnonymous == false) { "Administrator authentication required" }
        require(pickup.isNotBlank()) { "Pickup is required" }
        require(drop.isNotBlank()) { "Drop is required" }

        val otp = buildOtp()
        val tripId = hashOtp(otp)
        val ref = firestore.collection(COLLECTION).document(tripId)
        val tripRef = tripReference.ifBlank {
            "TRIP-" + UUID.randomUUID().toString().take(6).uppercase()
        }

        ref.set(hashMapOf<String, Any>(
            "assignmentId" to tripId,
            "tripReference" to tripRef,
            "customerName" to customerName,
            "customerMobile" to customerMobile,
            "pickup" to pickup,
            "drop" to drop,
            "rideMode" to rideMode,
            "otpHash" to tripId,
            "status" to STATUS_ASSIGNED,
            "createdAt" to FieldValue.serverTimestamp()
        )).await()

        Pair(
            LoadedTripAssignment(tripId, "", "", tripRef, customerName, customerMobile, pickup, drop, rideMode, STATUS_ASSIGNED),
            otp
        )
    }

    // Universal OTP claim. Firestore transaction makes the first driver win.
    suspend fun claimByOtp(otp: String, deviceId: String): Result<LoadedTripAssignment?> = runCatching {
        val user = auth.currentUser ?: auth.signInAnonymously().await().user
        requireNotNull(user) { "Unable to authenticate this device" }
        require(user.isAnonymous) { "Driver device authentication required" }

        val cleanOtp = otp.filter(Char::isDigit)
        if (cleanOtp.length != OTP_LENGTH) return@runCatching null

        val tripId = hashOtp(cleanOtp)
        val ref = firestore.collection(COLLECTION).document(tripId)

        firestore.runTransaction { transaction ->
            val latest = transaction.get(ref)
            if (!latest.exists()) throw IllegalStateException("Trip not found or OTP is invalid")
            if (latest.getString("status") != STATUS_ASSIGNED) {
                throw IllegalStateException("This trip has already been loaded")
            }
            transaction.update(ref, mapOf(
                "status" to STATUS_CLAIMED,
                "claimedAt" to FieldValue.serverTimestamp(),
                "claimedByUid" to user.uid,
                "ownerUid" to user.uid,
                "deviceId" to deviceId
            ))
            null
        }.await()

        val doc = ref.get().await()
        LoadedTripAssignment(
            assignmentId = tripId,
            deviceId = deviceId,
            ownerUid = user.uid,
            tripReference = doc.getString("tripReference").orEmpty(),
            customerName = doc.getString("customerName").orEmpty(),
            customerMobile = doc.getString("customerMobile").orEmpty(),
            pickup = doc.getString("pickup").orEmpty(),
            drop = doc.getString("drop").orEmpty(),
            rideMode = doc.getString("rideMode") ?: "CITY_RIDE",
            status = STATUS_CLAIMED
        )
    }

    suspend fun markStarted(assignmentId: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw IllegalStateException("Driver authentication required")
        require(user.isAnonymous) { "Driver device authentication required" }
        val ref = firestore.collection(COLLECTION).document(assignmentId)
        firestore.runTransaction { transaction ->
            val latest = transaction.get(ref)
            require(latest.exists()) { "Trip assignment not found" }
            require(latest.getString("ownerUid") == user.uid) { "Trip assignment belongs to another device" }
            require(latest.getString("status") == STATUS_CLAIMED) { "Trip is not in CLAIMED state" }
            transaction.update(ref, mapOf(
                "status" to "STARTED",
                "startedAt" to FieldValue.serverTimestamp(),
                "startedByUid" to user.uid
            ))
            null
        }.await()
    }

    suspend fun markCompleted(assignmentId: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw IllegalStateException("Driver authentication required")
        require(user.isAnonymous) { "Driver device authentication required" }
        val ref = firestore.collection(COLLECTION).document(assignmentId)
        firestore.runTransaction { transaction ->
            val latest = transaction.get(ref)
            require(latest.exists()) { "Trip assignment not found" }
            require(latest.getString("ownerUid") == user.uid) { "Trip assignment belongs to another device" }
            require(latest.getString("status") == "STARTED") { "Trip is not in STARTED state" }
            transaction.update(ref, mapOf(
                "status" to "COMPLETED",
                "completedAt" to FieldValue.serverTimestamp(),
                "completedByUid" to user.uid
            ))
            null
        }.await()
    }

    private fun buildOtp(): String = (100000 + secureRandom.nextInt(900000)).toString()
}

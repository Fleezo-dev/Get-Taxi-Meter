package com.example.data

import com.example.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

data class DeviceInstallation(
    val deviceId: String,
    val ownerUid: String,
    val driverName: String,
    val mobileNumber: String,
    val vehicleNumber: String,
    val vehicleType: String,
    val activationStatus: String,
    val firstSeenAt: Long?,
    val lastSeenAt: Long?,
    val activatedAt: Long?,
    val appVersion: String,
    val platform: String
)

class InstallationRepository {

    companion object {
        private const val COLLECTION = "app_installations"
    }

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Registers the current app installation and refreshes its last-seen timestamp.
     *
     * Driver/test devices use Firebase anonymous authentication so they can report
     * first-seen/last-seen information without exposing the administrator's login.
     * If the Master Admin is already signed in, that authenticated session is reused.
     *
     * Important: do not read the document before writing. Anonymous driver sessions
     * are intentionally not granted a general read permission for installation
     * documents. The write path therefore uses create-first, then update when the
     * document already exists.
     */
    suspend fun registerOrUpdate(
        deviceId: String,
        activationStatus: String,
        driverName: String,
        mobileNumber: String,
        vehicleNumber: String,
        vehicleType: String
    ): Result<Unit> {
        return runCatching {
            val user = auth.currentUser ?: auth.signInAnonymously().await().user
            requireNotNull(user) { "Unable to create installation authentication session" }

            val ref = firestore.collection(COLLECTION).document(deviceId)

            val baseData = hashMapOf<String, Any>(
                "deviceId" to deviceId,
                "ownerUid" to user.uid,
                "activationStatus" to activationStatus,
                "driverName" to driverName,
                "mobileNumber" to mobileNumber,
                "vehicleNumber" to vehicleNumber,
                "vehicleType" to vehicleType,
                "lastSeenAt" to FieldValue.serverTimestamp(),
                "appVersion" to BuildConfig.VERSION_NAME,
                "platform" to "Android"
            )

            try {
                val createData = HashMap(baseData)
                createData["firstSeenAt"] = FieldValue.serverTimestamp()
                ref.set(createData).await()
            } catch (createError: Exception) {
                // A document for this device already exists. Update is permitted
                // only when the existing record belongs to this authenticated device.
                ref.set(baseData, SetOptions.merge()).await()
            }
        }
    }

    suspend fun markActivated(
        deviceId: String,
        driverName: String,
        mobileNumber: String,
        vehicleNumber: String,
        vehicleType: String
    ): Result<Unit> {
        return runCatching {
            ensureAuthenticated()
            firestore.collection(COLLECTION).document(deviceId)
                .set(
                    mapOf(
                        "deviceId" to deviceId,
                        "activationStatus" to "ACTIVE",
                        "driverName" to driverName,
                        "mobileNumber" to mobileNumber,
                        "vehicleNumber" to vehicleNumber,
                        "vehicleType" to vehicleType,
                        "activatedAt" to FieldValue.serverTimestamp(),
                        "lastSeenAt" to FieldValue.serverTimestamp(),
                        "appVersion" to BuildConfig.VERSION_NAME,
                        "platform" to "Android"
                    ),
                    SetOptions.merge()
                ).await()
        }
    }

    suspend fun getAllInstallations(): Result<List<DeviceInstallation>> {
        return runCatching {
            val user = auth.currentUser
                ?: throw IllegalStateException("Administrator is not signed in")
            if (user.isAnonymous) {
                throw IllegalStateException("Administrator authentication required")
            }

            firestore.collection(COLLECTION).get().await().documents.map { doc ->
                DeviceInstallation(
                    deviceId = doc.getString("deviceId") ?: doc.id,
                    ownerUid = doc.getString("ownerUid").orEmpty(),
                    driverName = doc.getString("driverName").orEmpty(),
                    mobileNumber = doc.getString("mobileNumber").orEmpty(),
                    vehicleNumber = doc.getString("vehicleNumber").orEmpty(),
                    vehicleType = doc.getString("vehicleType").orEmpty(),
                    activationStatus = doc.getString("activationStatus") ?: "NOT_ACTIVATED",
                    firstSeenAt = doc.getTimestamp("firstSeenAt")?.toDate()?.time,
                    lastSeenAt = doc.getTimestamp("lastSeenAt")?.toDate()?.time,
                    activatedAt = doc.getTimestamp("activatedAt")?.toDate()?.time,
                    appVersion = doc.getString("appVersion").orEmpty(),
                    platform = doc.getString("platform") ?: "Android"
                )
            }.sortedByDescending { it.lastSeenAt ?: 0L }
        }
    }

    private suspend fun ensureAuthenticated() {
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
    }
}

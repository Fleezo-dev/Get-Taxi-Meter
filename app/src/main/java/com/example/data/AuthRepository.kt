package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

enum class AppRole {
    MASTER_ADMIN,
    ADMIN,
    DRIVER
}

data class AuthProfile(
    val uid: String,
    val email: String,
    val role: AppRole,
    val displayName: String = "",
    val active: Boolean = true
)

sealed interface AuthResult {
    data class Success(val profile: AuthProfile) : AuthResult
    data class Error(val message: String) : AuthResult
}

class AuthRepository {
    companion object {
        const val MASTER_ADMIN_EMAIL = "basheer222@gmail.com"
        private const val USERS = "users"
    }

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Anonymous Firebase sessions are used only for installation tracking; they must
    // never count as an administrator/driver application login.
    fun isSignedIn(): Boolean = auth.currentUser?.isAnonymous == false

    suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            if (auth.currentUser?.isAnonymous == true) {
                auth.signOut()
            }
            auth.signInWithEmailAndPassword(email.trim().lowercase(), password).await()
            loadOrBootstrapProfile()
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Unable to sign in")
        }
    }

    suspend fun loadOrBootstrapProfile(): AuthResult {
        val user = auth.currentUser ?: return AuthResult.Error("Not signed in")
        val uid = user.uid
        val email = user.email?.trim()?.lowercase()
            ?: return AuthResult.Error("Firebase account has no email address")

        return try {
            val ref = firestore.collection(USERS).document(uid)
            val snapshot = ref.get().await()

            if (snapshot.exists()) {
                val roleText = snapshot.getString("role")
                    ?: return AuthResult.Error("Account role is missing")
                val role = runCatching { AppRole.valueOf(roleText) }
                    .getOrElse { return AuthResult.Error("Unknown account role: $roleText") }
                val active = snapshot.getBoolean("active") ?: true
                if (!active) {
                    auth.signOut()
                    return AuthResult.Error("This account has been disabled")
                }
                return AuthResult.Success(
                    AuthProfile(
                        uid = uid,
                        email = email,
                        role = role,
                        displayName = snapshot.getString("displayName").orEmpty(),
                        active = active
                    )
                )
            }

            if (email == MASTER_ADMIN_EMAIL) {
                val data = mapOf(
                    "email" to email,
                    "displayName" to "Basheer",
                    "role" to AppRole.MASTER_ADMIN.name,
                    "active" to true,
                    "accountType" to "MASTER_ADMIN",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                ref.set(data).await()
                return AuthResult.Success(
                    AuthProfile(uid, email, AppRole.MASTER_ADMIN, "Basheer", true)
                )
            }

            auth.signOut()
            AuthResult.Error("Your Firebase account is not provisioned for Get Taxi Meter")
        } catch (e: Exception) {
            AuthResult.Error(
                "Signed in, but the account profile could not be loaded. " +
                    "Check Firestore Security Rules. ${e.localizedMessage.orEmpty()}"
            )
        }
    }

    fun signOut() = auth.signOut()
}

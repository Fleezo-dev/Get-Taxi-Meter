package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class DriverProfile(
    val driverId: String = "",
    val name: String = "",
    val mobileNumber: String = "",
    val vehicleNumber: String = "",
    val vehicleType: String = "Sedan",
    val photoPath: String? = null,
    val isRegistered: Boolean = false
) {
    val isValid: Boolean
        get() = name.isNotBlank() &&
                mobileNumber.isNotBlank() &&
                vehicleNumber.isNotBlank() &&
                vehicleType.isNotBlank()
}

class DriverProfileRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("gtm_driver_profile_prefs", Context.MODE_PRIVATE)

    private val _profile = MutableStateFlow(loadProfile())
    val profile: StateFlow<DriverProfile> = _profile.asStateFlow()

    fun loadProfile(): DriverProfile {
        val isRegistered = prefs.getBoolean("is_registered", false)
        val driverId = prefs.getString("driver_id", "") ?: ""
        val name = prefs.getString("driver_name", "") ?: ""
        val mobile = prefs.getString("mobile_number", "") ?: ""
        val vehicleNumber = prefs.getString("vehicle_number", "") ?: ""
        val vehicleType = prefs.getString("vehicle_type", "Sedan") ?: "Sedan"
        val photoPath = prefs.getString("photo_path", null)

        return DriverProfile(
            driverId = if (driverId.isBlank()) UUID.randomUUID().toString() else driverId,
            name = name,
            mobileNumber = mobile,
            vehicleNumber = vehicleNumber,
            vehicleType = vehicleType,
            photoPath = photoPath,
            isRegistered = isRegistered && name.isNotBlank()
        )
    }

    fun saveProfile(
        name: String,
        mobileNumber: String,
        vehicleNumber: String,
        vehicleType: String,
        photoPath: String?
    ): Boolean {
        if (name.isBlank() || mobileNumber.isBlank() || vehicleNumber.isBlank() || vehicleType.isBlank()) {
            return false
        }

        var currentId = prefs.getString("driver_id", null)
        if (currentId.isNullOrBlank()) {
            currentId = "DRV-" + UUID.randomUUID().toString().substring(0, 8).uppercase()
        }

        prefs.edit()
            .putString("driver_id", currentId)
            .putString("driver_name", name.trim())
            .putString("mobile_number", mobileNumber.trim())
            .putString("vehicle_number", vehicleNumber.trim().uppercase())
            .putString("vehicle_type", vehicleType.trim())
            .putString("photo_path", photoPath)
            .putBoolean("is_registered", true)
            .apply()

        _profile.value = DriverProfile(
            driverId = currentId,
            name = name.trim(),
            mobileNumber = mobileNumber.trim(),
            vehicleNumber = vehicleNumber.trim().uppercase(),
            vehicleType = vehicleType.trim(),
            photoPath = photoPath,
            isRegistered = true
        )
        return true
    }

    fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (bitmap == null) return null

            val file = File(context.filesDir, "driver_profile_photo.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

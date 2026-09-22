package com.example.data

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import java.security.SecureRandom
import java.util.Locale

object DeviceIdManager {

    private const val PREFS_NAME = "gtm_device_prefs"
    private const val KEY_DEVICE_ID = "gtm_unique_device_id"

    /**
     * Retrieves or generates a persistent, unique Get Taxi Meter Device ID.
     * Format: GTM-XXXX-XXXX-XXXX (e.g., GTM-7F4A-92B1-5E8D)
     * Does NOT use IMEI or restricted hardware identifiers.
     */
    @Synchronized
    fun getDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existingId = prefs.getString(KEY_DEVICE_ID, null)
        if (!existingId.isNullOrBlank()) {
            return existingId
        }

        val newId = generateSecureDeviceId()
        prefs.edit().putString(KEY_DEVICE_ID, newId).apply()
        return newId
    }

    /**
     * Generates a high-entropy 12-character alphanumeric code formatted as GTM-XXXX-XXXX-XXXX.
     */
    private fun generateSecureDeviceId(): String {
        val random = SecureRandom()
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // Excludes ambiguous chars: 0, 1, I, O
        fun segment(): String {
            val sb = StringBuilder(4)
            for (i in 0 until 4) {
                sb.append(chars[random.nextInt(chars.length)])
            }
            return sb.toString()
        }
        return "GTM-${segment()}-${segment()}-${segment()}".uppercase(Locale.US)
    }

    /**
     * Copies the Device ID to the system clipboard and notifies the user.
     */
    fun copyToClipboard(context: Context, deviceId: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Get Taxi Meter Device ID", deviceId)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Device ID copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}

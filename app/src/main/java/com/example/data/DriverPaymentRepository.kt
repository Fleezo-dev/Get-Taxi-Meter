package com.example.data

import android.content.Context

class DriverPaymentRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("gtm_driver_payment_prefs", Context.MODE_PRIVATE)

    fun getQrPath(): String? = prefs.getString("payment_qr_path", null)

    fun saveQrPath(path: String?) {
        prefs.edit().putString("payment_qr_path", path).apply()
    }
}

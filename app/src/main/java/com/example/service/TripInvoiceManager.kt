package com.example.service

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.DriverProfile
import com.example.model.TripState
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TripInvoiceManager {
    private const val COMPANY = "GET TAXI KOVAI"
    private const val ADDRESS = "No. 286, D.B. Road, R.S. Puram, Coimbatore - 641002, Tamil Nadu, India"
    private const val MOBILE = "90437 43777"
    private const val WEBSITES = "www.gettaxikovai.in  •  www.GetCabs.in"

    fun generatePdf(context: Context, trip: TripState, driver: DriverProfile, paymentQrPath: String?): Uri? {
        return try {
            val datePart = SimpleDateFormat("yyMMdd", Locale.US).format(Date(trip.startTimestamp))
            val driverId = if (driver.driverId.isBlank()) "0000" else driver.driverId
            val invoiceNo = "GTK-" + datePart + "-" + driverId + "-" + trip.tripId.toString().padStart(4, '0')
            val pickup = addressFor(context, trip.startLatitude, trip.startLongitude)
            val drop = addressFor(context, trip.currentLatitude, trip.currentLongitude)
            val document = PdfDocument()
            val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
            val canvas = page.canvas
            val black = Color.rgb(20, 20, 20)
            val red = Color.rgb(190, 24, 24)
            val gray = Color.rgb(95, 95, 95)
            val light = Color.rgb(245, 245, 245)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            fun text(value: String, x: Float, y: Float, size: Float, color: Int = black, bold: Boolean = false) {
                paint.color = color
                paint.textSize = size
                paint.typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
                canvas.drawText(value, x, y, paint)
            }
            fun line(y: Float) {
                paint.color = Color.rgb(225, 225, 225)
                paint.strokeWidth = 1f
                canvas.drawLine(42f, y, 553f, y, paint)
            }
            fun row(label: String, value: String, y: Float) {
                text(label, 52f, y, 10f, gray)
                paint.textSize = 10.5f
                text(value, 545f - paint.measureText(value), y, 10.5f, black, true)
            }

            paint.color = red
            canvas.drawRect(0f, 0f, 595f, 10f, paint)
            val logo = BitmapFactory.decodeResource(context.resources, context.applicationInfo.icon)
            if (logo != null) canvas.drawBitmap(Bitmap.createScaledBitmap(logo, 58, 58, true), null, android.graphics.Rect(42, 28, 100, 86), paint)
            text(COMPANY, 112f, 53f, 22f, black, true)
            text("Professional Taxi & Travel Services", 112f, 73f, 10.5f, gray)
            text("TRIP INVOICE", 420f, 48f, 11f, red, true)
            text(invoiceNo, 420f, 66f, 9f, gray)
            text(SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(trip.startTimestamp)), 420f, 82f, 9f, gray)
            line(104f)

            text("TRIP DETAILS", 42f, 130f, 10f, red, true)
            text("Pickup", 52f, 154f, 9.5f, gray); text(pickup, 150f, 154f, 10.5f, black, true)
            text("Pickup Time", 52f, 176f, 9.5f, gray); text(SimpleDateFormat("hh:mm a", Locale.US).format(Date(trip.startTimestamp)), 150f, 176f, 10.5f, black, true)
            text("Drop", 52f, 198f, 9.5f, gray); text(drop, 150f, 198f, 10.5f, black, true)
            text("Drop Time", 52f, 220f, 9.5f, gray); text(SimpleDateFormat("hh:mm a", Locale.US).format(Date(if (trip.endTimestamp != null) trip.endTimestamp else System.currentTimeMillis())), 150f, 220f, 10.5f, black, true)
            text("Distance Travelled", 52f, 242f, 9.5f, gray); text(String.format(Locale.US, "%.2f km", trip.distanceKm), 150f, 242f, 10.5f, black, true)
            line(262f)

            text("FARE SUMMARY", 42f, 290f, 10f, red, true)
            row("Base / Minimum Fare", String.format(Locale.US, "₹%.2f", trip.breakdown.baseFare), 318f)
            row("Distance Charges", String.format(Locale.US, "₹%.2f", trip.breakdown.distanceFare), 342f)
            row("Waiting Charges", String.format(Locale.US, "₹%.2f", trip.breakdown.waitingFare), 366f)
            if (trip.breakdown.extraChargesTotal > 0) row("Additional Charges", String.format(Locale.US, "₹%.2f", trip.breakdown.extraChargesTotal), 390f)
            paint.color = light; canvas.drawRoundRect(42f, 412f, 553f, 470f, 12f, 12f, paint)
            text("TOTAL FARE", 58f, 447f, 14f, black, true)
            val totalText = String.format(Locale.US, "₹%.2f", trip.breakdown.totalFare)
            paint.textSize = 24f; text(totalText, 545f - paint.measureText(totalText), 450f, 24f, red, true)

            text("DRIVER", 42f, 505f, 10f, red, true)
            row("Driver Name", if (driver.name.isBlank()) "—" else driver.name, 530f)
            row("Driver ID", driverId, 553f)
            row("Vehicle", driver.vehicleNumber + " • " + driver.vehicleType, 576f)

            val qr = if (paymentQrPath != null) BitmapFactory.decodeFile(paymentQrPath) else null
            if (qr != null) {
                text("PAYMENT", 420f, 505f, 10f, red, true)
                canvas.drawBitmap(Bitmap.createScaledBitmap(qr, 92, 92, true), null, android.graphics.Rect(420, 518, 512, 610), paint)
                text("Scan to pay driver", 420f, 624f, 8.5f, gray)
            }

            line(700f)
            text(COMPANY, 42f, 728f, 11f, black, true)
            text(ADDRESS, 42f, 747f, 8.5f, gray)
            text("Mobile: " + MOBILE, 42f, 766f, 8.5f, gray)
            text(WEBSITES, 42f, 785f, 8.5f, gray)
            text("Thank you for travelling with us.", 42f, 812f, 9.5f, red, true)
            document.finishPage(page)

            val safeName = invoiceNo.replace("[^A-Za-z0-9-]".toRegex(), "_") + ".pdf"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, safeName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Get Taxi")
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
                context.contentResolver.openOutputStream(uri)?.use { document.writeTo(it) }
                document.close()
                uri
            } else {
                val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Get Taxi"); dir.mkdirs()
                val file = File(dir, safeName)
                FileOutputStream(file).use { document.writeTo(it) }; document.close()
                FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
            }
        } catch (_: Exception) { null }
    }

    fun sharePdf(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Trip Invoice"))
    }

    private fun addressFor(context: Context, lat: Double?, lon: Double?): String {
        if (lat == null || lon == null) return "Location unavailable"
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val results = geocoder.getFromLocation(lat, lon, 1)
            results?.firstOrNull()?.getAddressLine(0) ?: String.format(Locale.US, "%.5f, %.5f", lat, lon)
        } catch (_: Exception) { String.format(Locale.US, "%.5f, %.5f", lat, lon) }
    }
}

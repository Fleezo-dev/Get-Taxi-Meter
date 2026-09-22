package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.example.MainActivity
import com.example.model.TripState
import com.example.model.TripStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

object FloatingOverlayManager {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var isOverlayShowing = false

    // UI references within the overlay
    private var fareTextView: TextView? = null
    private var statsTextView: TextView? = null
    private var statusTextView: TextView? = null

    private var scope: CoroutineScope? = null
    private var observerJob: Job? = null

    private const val PREFS_NAME = "gtm_overlay_prefs"
    private const val KEY_OVERLAY_ENABLED = "overlay_enabled"

    fun isOverlayEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_OVERLAY_ENABLED, false)
    }

    fun setOverlayEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_OVERLAY_ENABLED, enabled).apply()
        if (!enabled) {
            hideOverlay(context)
        } else {
            val currentState = TaxiMeterService.tripState.value
            if (currentState.status == TripStatus.ACTIVE || currentState.status == TripStatus.WAITING) {
                showOverlay(context)
            }
        }
    }

    fun canDrawOverlay(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Synchronized
    fun showOverlay(context: Context) {
        if (isOverlayShowing || !canDrawOverlay(context)) return

        val app = context.applicationContext
        windowManager = app.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 150
        }
        layoutParams = params

        val rootContainer = createOverlayLayout(app)
        overlayView = rootContainer

        // Dragging & Click Handling
        rootContainer.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isDragging = false

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isDragging = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            isDragging = true
                        }
                        params.x = initialX + dx
                        params.y = initialY + dy
                        try {
                            windowManager?.updateViewLayout(overlayView, params)
                        } catch (_: Exception) {}
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isDragging) {
                            // Tapped: Bring existing trip activity to front without resetting trip
                            val intent = Intent(app, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                            app.startActivity(intent)
                        }
                        return true
                    }
                }
                return false
            }
        })

        try {
            windowManager?.addView(rootContainer, params)
            isOverlayShowing = true
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        // Single Source of Truth: Collect TaxiMeterService.tripState
        scope = CoroutineScope(Dispatchers.Main)
        observerJob = scope?.launch {
            TaxiMeterService.tripState.collectLatest { state ->
                if (state.status != TripStatus.ACTIVE && state.status != TripStatus.WAITING) {
                    hideOverlay(context)
                } else {
                    updateOverlayUi(state)
                }
            }
        }
    }

    private fun updateOverlayUi(state: TripState) {
        val fare = String.format(Locale.US, "₹%.2f", state.breakdown.totalFare)
        fareTextView?.text = fare

        val dist = String.format(Locale.US, "%.2f km", state.distanceKm)
        val wait = TaxiMeterService.formatDuration(state.waitingDurationSeconds)
        statsTextView?.text = "$dist • $wait"

        if (state.isMoving) {
            statusTextView?.text = "DRIVING"
            statusTextView?.setTextColor(Color.parseColor("#10B981")) // Green
        } else {
            statusTextView?.text = "WAITING"
            statusTextView?.setTextColor(Color.parseColor("#F59E0B")) // Amber
        }
    }

    @Synchronized
    fun hideOverlay(context: Context) {
        observerJob?.cancel()
        observerJob = null
        scope = null

        if (isOverlayShowing && overlayView != null) {
            try {
                windowManager?.removeView(overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            overlayView = null
            fareTextView = null
            statsTextView = null
            statusTextView = null
            isOverlayShowing = false
        }
    }

    private fun createOverlayLayout(context: Context): View {
        fun dp(value: Float): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                context.resources.displayMetrics
            ).toInt()
        }

        val cardBg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(14f).toFloat()
            setColor(Color.parseColor("#111827")) // Dark sleek container
            setStroke(dp(1.5f), Color.parseColor("#E51E25")) // Red brand accent
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = cardBg
            setPadding(dp(12f), dp(8f), dp(12f), dp(8f))
            gravity = Gravity.CENTER_HORIZONTAL
            elevation = dp(8f).toFloat()
        }

        // Top Row: Brand & Status
        val headerRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = TextView(context).apply {
            text = "GET TAXI"
            textSize = 9.5f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#F3F4F6"))
        }

        val dot = TextView(context).apply {
            text = " • "
            textSize = 9.5f
            setTextColor(Color.parseColor("#9CA3AF"))
        }

        val status = TextView(context).apply {
            text = "ACTIVE"
            textSize = 9.5f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#10B981"))
        }
        statusTextView = status

        headerRow.addView(title)
        headerRow.addView(dot)
        headerRow.addView(status)
        container.addView(headerRow)

        // Fare Display
        val fare = TextView(context).apply {
            text = "₹0.00"
            textSize = 19f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, dp(1f), 0, dp(1f))
        }
        fareTextView = fare
        container.addView(fare)

        // Subtitle: Distance & Waiting time
        val stats = TextView(context).apply {
            text = "0.00 km • 00:00:00"
            textSize = 10.5f
            setTextColor(Color.parseColor("#D1D5DB"))
            gravity = Gravity.CENTER
        }
        statsTextView = stats
        container.addView(stats)

        return container
    }
}

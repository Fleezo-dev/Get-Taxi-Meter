package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppCardBorder
import com.example.ui.theme.AppCardSecondary
import com.example.ui.theme.AppWhiteBg
import com.example.ui.theme.BrandRed
import com.example.ui.theme.BrandYellow
import com.example.ui.theme.MeterAmber
import com.example.ui.theme.MeterAmberBg
import com.example.ui.theme.MeterGreen
import com.example.ui.theme.MeterGreenBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

/**
 * High-fidelity vector illustration of the taxi front view
 * matching the exact reference logo:
 * - Yellow TAXI roof sign
 * - Dynamic red curved swooshes flanking the vehicle on the left and right
 * - Sleek black car front with windshield, headlights, and front grille
 */
@Composable
fun TaxiFrontIllustration(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .width(180.dp)
            .height(82.dp)
    ) {
        val w = size.width
        val h = size.height

        // 1. Red Dynamic Curved Wings / Swooshes flanking the vehicle
        val leftRedWing = Path().apply {
            moveTo(w * 0.28f, h * 0.18f)
            cubicTo(w * 0.10f, h * 0.25f, w * 0.02f, h * 0.50f, w * 0.08f, h * 0.85f)
            cubicTo(w * 0.12f, h * 0.92f, w * 0.22f, h * 0.88f, w * 0.20f, h * 0.80f)
            cubicTo(w * 0.14f, h * 0.56f, w * 0.18f, h * 0.38f, w * 0.32f, h * 0.26f)
            close()
        }
        drawPath(path = leftRedWing, color = BrandRed)

        val rightRedWing = Path().apply {
            moveTo(w * 0.72f, h * 0.18f)
            cubicTo(w * 0.90f, h * 0.25f, w * 0.98f, h * 0.50f, w * 0.92f, h * 0.85f)
            cubicTo(w * 0.88f, h * 0.92f, w * 0.78f, h * 0.88f, w * 0.80f, h * 0.80f)
            cubicTo(w * 0.86f, h * 0.56f, w * 0.82f, h * 0.38f, w * 0.68f, h * 0.26f)
            close()
        }
        drawPath(path = rightRedWing, color = BrandRed)

        // 2. Black Shadow Base under tires
        drawOval(
            color = Color(0x33000000),
            topLeft = Offset(w * 0.18f, h * 0.88f),
            size = Size(w * 0.64f, h * 0.10f)
        )

        // 3. TAXI Roof Sign (Yellow pill trapezoid on roof)
        val roofSignPath = Path().apply {
            moveTo(w * 0.41f, h * 0.02f)
            lineTo(w * 0.59f, h * 0.02f)
            lineTo(w * 0.62f, h * 0.22f)
            lineTo(w * 0.38f, h * 0.22f)
            close()
        }
        drawPath(path = roofSignPath, color = BrandYellow, style = Fill)
        drawPath(path = roofSignPath, color = Color.Black, style = Stroke(width = 1.5f))

        // Roof sign black base
        drawRect(
            color = Color.Black,
            topLeft = Offset(w * 0.36f, h * 0.21f),
            size = Size(w * 0.28f, h * 0.04f)
        )

        // 4. Car Cabin & Windshield (Aerodynamic silhouette)
        val cabinPath = Path().apply {
            moveTo(w * 0.33f, h * 0.25f)
            quadraticTo(w * 0.50f, h * 0.16f, w * 0.67f, h * 0.25f)
            lineTo(w * 0.77f, h * 0.52f)
            quadraticTo(w * 0.50f, h * 0.50f, w * 0.23f, h * 0.52f)
            close()
        }
        drawPath(path = cabinPath, color = Color(0xFF0F172A), style = Fill)

        // Windshield Glass Highlight
        val glassPath = Path().apply {
            moveTo(w * 0.36f, h * 0.28f)
            quadraticTo(w * 0.50f, h * 0.22f, w * 0.64f, h * 0.28f)
            lineTo(w * 0.72f, h * 0.49f)
            quadraticTo(w * 0.50f, h * 0.47f, w * 0.28f, h * 0.49f)
            close()
        }
        drawPath(path = glassPath, color = Color(0xFF1E293B), style = Fill)

        // Passenger / Driver silhouettes inside windshield
        drawCircle(color = Color(0xFF0F172A), radius = w * 0.024f, center = Offset(w * 0.44f, h * 0.36f))
        drawCircle(color = Color(0xFF0F172A), radius = w * 0.024f, center = Offset(w * 0.56f, h * 0.36f))

        // 5. Car Hood & Front Bumper
        val hoodPath = Path().apply {
            moveTo(w * 0.21f, h * 0.51f)
            quadraticTo(w * 0.50f, h * 0.47f, w * 0.79f, h * 0.51f)
            quadraticTo(w * 0.85f, h * 0.62f, w * 0.82f, h * 0.82f)
            quadraticTo(w * 0.80f, h * 0.92f, w * 0.70f, h * 0.94f)
            quadraticTo(w * 0.50f, h * 0.96f, w * 0.30f, h * 0.94f)
            quadraticTo(w * 0.20f, h * 0.92f, w * 0.18f, h * 0.82f)
            quadraticTo(w * 0.15f, h * 0.62f, w * 0.21f, h * 0.51f)
            close()
        }
        drawPath(path = hoodPath, color = Color(0xFF0F172A), style = Fill)

        // 6. Dual Glowing Headlights
        // Left headlight
        drawOval(
            color = Color.White,
            topLeft = Offset(w * 0.22f, h * 0.62f),
            size = Size(w * 0.12f, h * 0.12f)
        )
        drawOval(
            color = Color(0xFFE2E8F0),
            topLeft = Offset(w * 0.24f, h * 0.64f),
            size = Size(w * 0.08f, h * 0.08f)
        )

        // Right headlight
        drawOval(
            color = Color.White,
            topLeft = Offset(w * 0.66f, h * 0.62f),
            size = Size(w * 0.12f, h * 0.12f)
        )
        drawOval(
            color = Color(0xFFE2E8F0),
            topLeft = Offset(w * 0.68f, h * 0.64f),
            size = Size(w * 0.08f, h * 0.08f)
        )

        // 7. Grille & Lower Bumper
        drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(w * 0.38f, h * 0.68f),
            size = Size(w * 0.24f, h * 0.16f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        // Grille horizontal lines
        drawLine(
            color = Color(0xFF475569),
            start = Offset(w * 0.40f, h * 0.73f),
            end = Offset(w * 0.60f, h * 0.73f),
            strokeWidth = 1.5f
        )
        drawLine(
            color = Color(0xFF475569),
            start = Offset(w * 0.41f, h * 0.78f),
            end = Offset(w * 0.59f, h * 0.78f),
            strokeWidth = 1.5f
        )
    }
}

/**
 * Main prominent branding component matching the visual target:
 * Taxi front graphic -> Get Taxi -> — METER — pill -> Drive • Track • Earn
 */
@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!compact) {
            // High-fidelity Taxi Front Illustration
            TaxiFrontIllustration()
            Spacer(modifier = Modifier.height(2.dp))
        } else {
            // Compact TAXI roof badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                    .background(BrandYellow)
                    .border(1.dp, Color(0xFF111827), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                    .padding(horizontal = 8.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "TAXI",
                    color = Color.Black,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
        }

        // "Get Taxi" Main Brand Typography
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Get",
                color = Color.Black,
                fontSize = if (compact) 22.sp else 34.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )
            Text(
                text = "Taxi",
                color = BrandRed,
                fontSize = if (compact) 22.sp else 34.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // "— METER —" Black pill badge with white text
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, BrandRed.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                .padding(horizontal = if (compact) 10.dp else 16.dp, vertical = 2.5.dp)
        ) {
            Text(
                text = "— M E T E R —",
                color = Color.White,
                fontSize = if (compact) 9.sp else 11.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }

        if (!compact) {
            Spacer(modifier = Modifier.height(10.dp))
            DriveTrackEarnTagline()
        }
    }
}

/**
 * Capsule pill tagline: "Drive  •  Track  •  Earn"
 */
@Composable
fun DriveTrackEarnTagline(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, BrandRed.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .padding(horizontal = 22.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Drive",
                color = Color(0xFF1E293B),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "•",
                color = BrandRed,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Track",
                color = Color(0xFF1E293B),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "•",
                color = BrandRed,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Earn",
                color = Color(0xFF1E293B),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Curved Red & Black footer matching the reference design:
 * "Get Taxi — M E T E R —" + "SAFE RIDES • TRANSPARENT FARES"
 */
@Composable
fun CurvedBrandFooter(
    modifier: Modifier = Modifier,
    compact: Boolean = true
) {
    val footerHeight = if (compact) 36.dp else 96.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(footerHeight)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Red wave arch on top
            val redPath = Path().apply {
                moveTo(0f, h * 0.35f)
                quadraticTo(w * 0.5f, 0f, w, h * 0.35f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(path = redPath, color = BrandRed)

            // Deep Black curve sitting just below the red arch
            val blackPath = Path().apply {
                moveTo(0f, h * 0.55f)
                quadraticTo(w * 0.5f, h * 0.18f, w, h * 0.55f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(path = blackPath, color = Color(0xFF0A0C10))
        }

        // Branding and Tagline content inside the footer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = if (compact) 3.dp else 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Get",
                    color = Color.White,
                    fontSize = if (compact) 11.sp else 17.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Taxi",
                    color = BrandRed,
                    fontSize = if (compact) 11.sp else 17.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E232E))
                        .border(1.dp, BrandRed.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = if (compact) 5.dp else 7.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "— METER —",
                        color = Color.White,
                        fontSize = if (compact) 7.5.sp else 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            if (!compact) {
                Spacer(modifier = Modifier.height(3.dp))
            }

            Text(
                text = "SAFE RIDES   •   TRANSPARENT FARES",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = if (compact) 7.5.sp else 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

/**
 * Motion and GPS status badge for active drive screen,
 * styled for clean white high-contrast cockpit.
 */
@Composable
fun MeterStatusBadge(
    isMoving: Boolean,
    speedKmH: Float,
    gpsAccuracyMeters: Float,
    gpsStatus: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, AppCardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Motion Badge
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isMoving) MeterGreen else MeterAmber)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = if (isMoving) "MOVING" else "WAITING / STOPPED",
                    color = if (isMoving) MeterGreen else MeterAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format(Locale.US, "%.1f km/h", speedKmH),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // GPS Accuracy Badge
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (gpsAccuracyMeters in 0.1f..35f) "GPS READY" else "ACQUIRING",
                    color = if (gpsAccuracyMeters in 0.1f..35f) MeterGreen else TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (gpsAccuracyMeters > 0) "Accuracy ±${gpsAccuracyMeters.toInt()} m" else "Searching...",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Active trip recovery banner if an unfinished trip is detected
 */
@Composable
fun ActiveTripRecoveryBanner(
    onDismiss: () -> Unit,
    onResumeTrip: () -> Unit,
    fareAmount: Double,
    distanceKm: Double
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MeterAmberBg)
            .border(1.5.dp, MeterAmber, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Active Trip Found",
                    tint = MeterAmber,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ACTIVE TRIP FOUND",
                    color = Color(0xFF78350F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "An unfinished trip is stored locally (₹${String.format(Locale.US, "%.2f", fareAmount)} • ${String.format(Locale.US, "%.2f", distanceKm)} km). Resume to continue background meter.",
                color = Color(0xFF92400E),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF78350F)),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(text = "Discard", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onResumeTrip,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed, contentColor = Color.White),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(text = "RESUME METER", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}


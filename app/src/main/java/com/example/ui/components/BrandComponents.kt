package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
 * Official Get Taxi Meter brand emblem vector asset
 * matching the launcher icon and the visual reference (ic_get_taxi_meter_brand):
 * - Red outer border with rounded white canvas
 * - Dynamic red road swoosh
 * - Yellow TAXI roof sign
 * - Taxi front silhouette with headlights
 * - Get Taxi typography and — METER — pill
 * - DRIVE • TRACK • EARN tagline
 */
@Composable
fun TaxiFrontIllustration(
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.ic_get_taxi_meter_brand),
        contentDescription = "Get Taxi Meter Logo",
        modifier = modifier
            .size(72.dp),
        contentScale = ContentScale.Fit
    )
}

/**
 * Main prominent branding component matching the official logo badge
 */
@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Image(
        painter = painterResource(id = R.drawable.ic_get_taxi_meter_brand),
        contentDescription = "Get Taxi Meter Logo",
        modifier = modifier
            .size(if (compact) 64.dp else 88.dp),
        contentScale = ContentScale.Fit
    )
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


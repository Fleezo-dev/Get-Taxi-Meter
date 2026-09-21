package com.example.ui.components

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppCardBorder
import com.example.ui.theme.AppCardSecondary
import com.example.ui.theme.AppWhiteBg
import com.example.ui.theme.BrandRed
import com.example.ui.theme.BrandYellow
import com.example.ui.theme.MeterAmber
import com.example.ui.theme.MeterGreen
import com.example.ui.theme.TextSecondary
import java.util.Locale

/**
 * CompactMeterDashboard
 * Fits the complete Get Taxi Meter interface inside ONE Android phone screen without vertical scrolling:
 * 1. Top Header (Menu, Get Taxi branding, Settings, Drive • Track • Earn)
 * 2. Status Row (GPS Ready, Driving / Vehicle status, Date / Time)
 * 3. Current Fare Card (Large fare number with Base/Dist/Wait breakdown)
 * 4. Three Metrics (Distance, Trip Time, Waiting Time)
 * 5. Extra Charges (Toll, Parking, Airport, Custom)
 * 6. Action Button (START TRIP / END TRIP)
 * 7. Quick Actions Row (Trip History, Tariff Settings, Today's Summary, More)
 * 8. Bottom Branding Footer (Curved Get Taxi — METER banner)
 */
@Composable
fun CompactMeterDashboard(
    isTripActive: Boolean,
    onActionClick: () -> Unit,
    gpsReady: Boolean,
    gpsAccuracyMeters: Float,
    isMoving: Boolean,
    currentSpeedKmH: Float,
    currentFare: Double,
    baseFare: Double,
    distanceFare: Double,
    waitingFare: Double,
    extraFare: Double,
    distanceKm: Double,
    ratePerKm: Double,
    tripDurationFormatted: String,
    waitingDurationFormatted: String,
    waitingRatePerMinute: Double,
    totalExtras: Double,
    dateText: String,
    timeText: String,
    onAddToll: () -> Unit,
    onAddParking: () -> Unit,
    onAddAirport: () -> Unit,
    onAddCustom: () -> Unit,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onShareClick: () -> Unit,
    onTripHistoryClick: () -> Unit,
    onTariffSettingsClick: () -> Unit,
    onTodaySummaryClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppWhiteBg)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. TOP HEADER & BRANDING
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT: ☰ Menu
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // CENTER: TAXI vehicle artwork + Get Taxi + — METER —
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Taxi Front Artwork with Yellow TAXI roof badge and red wings
                    TaxiFrontIllustration(
                        modifier = Modifier
                            .width(160.dp)
                            .height(64.dp)
                    )

                    // Get Taxi Typography
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Get",
                            color = Color.Black,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Taxi",
                            color = BrandRed,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    // — METER — badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.2.dp, BrandRed.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "— M E T E R —",
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                }

                // RIGHT: Share & ⚙ Settings
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Share",
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onShareClick() }
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                    )
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Drive • Track • Earn
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, BrandRed.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 2.5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "Drive", color = Color(0xFF1E293B), fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "•", color = BrandRed, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Text(text = "Track", color = Color(0xFF1E293B), fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "•", color = BrandRed, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Text(text = "Earn", color = Color(0xFF1E293B), fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // 2. STATUS ROW: GPS READY | DRIVING / READY | DATE & TIME
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card 1: GPS Ready
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .border(1.2.dp, AppCardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = AppWhiteBg),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (gpsReady) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (gpsReady) MeterGreen else MeterAmber)
                        )
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Column {
                        Text(
                            text = if (gpsReady) "GPS READY" else "SEARCHING",
                            color = if (gpsReady) MeterGreen else MeterAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        val accuracyStr = if (gpsAccuracyMeters > 0) "Accuracy ±${gpsAccuracyMeters.toInt()} m" else "Accuracy ±4 m"
                        Text(
                            text = accuracyStr,
                            color = TextSecondary,
                            fontSize = 8.5.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            // Card 2: DRIVING / READY TO DRIVE
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .border(1.2.dp, AppCardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = AppWhiteBg),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Driving Status",
                        tint = if (isTripActive && isMoving) MeterGreen else Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Column {
                        if (isTripActive) {
                            Text(
                                text = if (isMoving) "DRIVING" else "WAITING",
                                color = if (isMoving) MeterGreen else MeterAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = if (isMoving) "${currentSpeedKmH.toInt()} km/h" else "Stopped",
                                color = Color.Black,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        } else {
                            Text(
                                text = "READY",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "TO DRIVE",
                                color = TextSecondary,
                                fontSize = 8.5.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Card 3: DATE & TIME
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .border(1.2.dp, AppCardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = AppWhiteBg),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date and Time",
                        tint = Color.Black,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Column {
                        Text(
                            text = dateText,
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = timeText,
                            color = TextSecondary,
                            fontSize = 8.5.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 3. CURRENT FARE HERO CARD (Dominant element)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, BrandRed, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = AppWhiteBg),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CURRENT FARE",
                    color = TextSecondary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = String.format(Locale.US, "₹%.2f", currentFare),
                    color = Color.Black,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-0.8).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF1F3F5))
                        .padding(horizontal = 14.dp, vertical = 4.5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Base ₹${baseFare.toInt()}", color = Color(0xFF374151), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "|", color = Color(0xFFD1D5DB), fontSize = 11.sp)
                        Text(text = "Dist ₹${distanceFare.toInt()}", color = Color(0xFF374151), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "|", color = Color(0xFFD1D5DB), fontSize = 11.sp)
                        Text(text = "Wait ₹${waitingFare.toInt()}", color = Color(0xFF374151), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        if (extraFare > 0) {
                            Text(text = "|", color = Color(0xFFD1D5DB), fontSize = 11.sp)
                            Text(text = "Extra ₹${extraFare.toInt()}", color = BrandRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. THREE METRICS: DISTANCE | TRIP TIME | WAITING TIME
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricItemCompact(
                icon = Icons.Default.Navigation,
                title = "DISTANCE",
                value = String.format(Locale.US, "%.2f km", distanceKm),
                subtitle = "₹${ratePerKm.toInt()} / km",
                modifier = Modifier.weight(1f)
            )

            MetricItemCompact(
                icon = Icons.Default.Schedule,
                title = "TRIP TIME",
                value = tripDurationFormatted,
                subtitle = "Total elapsed",
                modifier = Modifier.weight(1f)
            )

            MetricItemCompact(
                icon = Icons.Default.Pause,
                title = "WAITING TIME",
                value = waitingDurationFormatted,
                subtitle = "₹${waitingRatePerMinute.toInt()} / min",
                highlight = isTripActive && !isMoving,
                modifier = Modifier.weight(1f)
            )
        }

        // 5. EXTRA CHARGES CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.2.dp, AppCardBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = AppWhiteBg),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EXTRA CHARGES",
                        color = Color.Black,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Total: ₹${String.format(Locale.US, "%.2f", totalExtras)}",
                        color = if (totalExtras > 0) BrandRed else Color.Black,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExtraChargeButtonCompact(
                        icon = Icons.Default.Toll,
                        title = "Toll",
                        amount = "+ ₹50",
                        onClick = onAddToll,
                        modifier = Modifier.weight(1f)
                    )
                    ExtraChargeButtonCompact(
                        icon = Icons.Default.LocalParking,
                        title = "Parking",
                        amount = "+ ₹30",
                        onClick = onAddParking,
                        modifier = Modifier.weight(1f)
                    )
                    ExtraChargeButtonCompact(
                        icon = Icons.Default.Flight,
                        title = "Airport",
                        amount = "+ ₹100",
                        onClick = onAddAirport,
                        modifier = Modifier.weight(1f)
                    )
                    ExtraChargeButtonCompact(
                        icon = Icons.Default.Add,
                        title = "Custom",
                        amount = "Add",
                        isAccent = true,
                        onClick = onAddCustom,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 6. ACTION BUTTON (START TRIP / END TRIP)
        Button(
            onClick = onActionClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandRed,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 1.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isTripActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isTripActive) "End Trip" else "Start Trip",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isTripActive) "END TRIP" else "START TRIP",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
            }
        }

        // 7. QUICK ACTIONS ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            QuickActionButtonCompact(
                icon = Icons.Default.History,
                label = "Trip History",
                onClick = onTripHistoryClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionButtonCompact(
                icon = Icons.Default.Settings,
                label = "Tariff Settings",
                onClick = onTariffSettingsClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionButtonCompact(
                icon = Icons.Default.Assessment,
                label = "Today's Summary",
                onClick = onTodaySummaryClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionButtonCompact(
                icon = Icons.Default.MoreHoriz,
                label = "More",
                onClick = onMoreClick,
                modifier = Modifier.weight(1f)
            )
        }

        // 8. BOTTOM BRANDING
        CurvedBrandFooter(
            compact = false
        )
    }
}

@Composable
fun MetricItemCompact(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(68.dp)
            .border(1.2.dp, AppCardBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = AppWhiteBg),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = BrandRed,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            Text(
                text = value,
                color = if (highlight) BrandRed else Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 8.5.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ExtraChargeButtonCompact(
    icon: ImageVector,
    title: String,
    amount: String,
    isAccent: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isAccent) Color(0xFFFEE2E2) else AppCardSecondary)
            .border(1.dp, if (isAccent) BrandRed.copy(alpha = 0.5f) else AppCardBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isAccent) BrandRed else Color(0xFF1E293B),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Column {
                Text(
                    text = title,
                    color = if (isAccent) BrandRed else Color.Black,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = amount,
                    color = if (isAccent) BrandRed else TextSecondary,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun QuickActionButtonCompact(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(48.dp)
            .border(1.2.dp, AppCardBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = AppWhiteBg),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.Black,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = Color.Black,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

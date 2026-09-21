package com.example.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.TaxiMeterService
import com.example.ui.components.CompactMeterDashboard
import com.example.ui.theme.AppCardBorder
import com.example.ui.theme.AppCardSecondary
import com.example.ui.theme.AppWhiteBg
import com.example.ui.theme.BrandRed
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MeterViewModel
import com.example.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LiveMeterScreen(
    viewModel: MeterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tripState by viewModel.tripState.collectAsState()
    val todayTrips by viewModel.todayCompletedTrips.collectAsState()

    var showEndTripConfirmation by remember { mutableStateOf(false) }
    var showAddExtraDialog by remember { mutableStateOf(false) }
    var showTodaySummaryDialog by remember { mutableStateOf(false) }
    var showMoreMenuDialog by remember { mutableStateOf(false) }

    // Dynamic current Date & Time
    val currentDate = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date())
    val currentTime = SimpleDateFormat("hh:mm a", Locale.US).format(Date())

    val todayEarnings = todayTrips.sumOf { it.currentFare }
    val todayDistanceKm = todayTrips.sumOf { it.totalDistanceMeters } / 1000.0

    CompactMeterDashboard(
        isTripActive = true,
        onActionClick = { showEndTripConfirmation = true },
        gpsReady = tripState.gpsAccuracyMeters > 0 && tripState.gpsStatus != "SEARCHING",
        gpsAccuracyMeters = tripState.gpsAccuracyMeters,
        isMoving = tripState.isMoving,
        currentSpeedKmH = tripState.currentSpeedKmH,
        currentFare = tripState.breakdown.totalFare,
        baseFare = tripState.breakdown.baseFare,
        distanceFare = tripState.breakdown.distanceFare,
        waitingFare = tripState.breakdown.waitingFare,
        extraFare = tripState.breakdown.extraChargesTotal,
        distanceKm = tripState.distanceKm,
        ratePerKm = tripState.tariff.distanceRatePerKm,
        tripDurationFormatted = TaxiMeterService.formatDuration(tripState.tripDurationSeconds),
        waitingDurationFormatted = TaxiMeterService.formatDuration(tripState.waitingDurationSeconds),
        waitingRatePerMinute = tripState.tariff.waitingRatePerMinute,
        totalExtras = tripState.breakdown.extraChargesTotal,
        dateText = currentDate,
        timeText = currentTime,
        onAddToll = { viewModel.addExtra(context, "Toll", 50.0) },
        onAddParking = { viewModel.addExtra(context, "Parking", 30.0) },
        onAddAirport = { viewModel.addExtra(context, "Airport", 100.0) },
        onAddCustom = { showAddExtraDialog = true },
        onMenuClick = { showMoreMenuDialog = true },
        onSettingsClick = { viewModel.navigateTo(Screen.SETTINGS) },
        onShareClick = { shareApp(context) },
        onTripHistoryClick = { viewModel.navigateTo(Screen.HISTORY) },
        onTariffSettingsClick = { viewModel.navigateTo(Screen.SETTINGS) },
        onTodaySummaryClick = { showTodaySummaryDialog = true },
        onMoreClick = { showMoreMenuDialog = true },
        modifier = modifier
    )

    // CONFIRMATION DIALOG BEFORE ENDING TRIP
    if (showEndTripConfirmation) {
        AlertDialog(
            onDismissRequest = { showEndTripConfirmation = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = "End this trip?",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to stop the meter and complete this trip?",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppCardSecondary)
                            .border(1.dp, AppCardBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "FINAL FARE",
                                color = TextSecondary,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format(Locale.US, "₹%.2f", tripState.breakdown.totalFare),
                                color = BrandRed,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.SansSerif
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.2f km", tripState.distanceKm)} • ${TaxiMeterService.formatDuration(tripState.tripDurationSeconds)}",
                                color = Color.Black,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEndTripConfirmation = false
                        viewModel.endTrip(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed, contentColor = Color.White)
                ) {
                    Text(text = "CONFIRM END TRIP", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showEndTripConfirmation = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    // CUSTOM EXTRA CHARGE DIALOG
    if (showAddExtraDialog) {
        var extraLabel by remember { mutableStateOf("") }
        var extraAmountText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddExtraDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Add Extra Charge",
                    color = Color.Black,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = extraLabel,
                        onValueChange = { extraLabel = it },
                        label = { Text("Description (e.g., Toll, Luggage)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = BrandRed,
                            unfocusedBorderColor = AppCardBorder
                        )
                    )

                    OutlinedTextField(
                        value = extraAmountText,
                        onValueChange = { extraAmountText = it },
                        label = { Text("Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = BrandRed,
                            unfocusedBorderColor = AppCardBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = extraAmountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            val label = if (extraLabel.isBlank()) "Extra" else extraLabel.trim()
                            viewModel.addExtra(context, label, amount)
                        }
                        showAddExtraDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed, contentColor = Color.White)
                ) {
                    Text(text = "Add Extra", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showAddExtraDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    // TODAY'S SUMMARY DIALOG
    if (showTodaySummaryDialog) {
        AlertDialog(
            onDismissRequest = { showTodaySummaryDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Today's Summary",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Trips Today", color = TextSecondary, fontSize = 13.sp)
                        Text("${todayTrips.size}", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Today's Earnings", color = TextSecondary, fontSize = 13.sp)
                        Text(String.format(Locale.US, "₹%.2f", todayEarnings), color = BrandRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Distance Covered", color = TextSecondary, fontSize = 13.sp)
                        Text(String.format(Locale.US, "%.2f km", todayDistanceKm), color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTodaySummaryDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed, contentColor = Color.White)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // MORE OPTIONS MENU DIALOG
    if (showMoreMenuDialog) {
        AlertDialog(
            onDismissRequest = { showMoreMenuDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Options & Tools",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Battery Guidance Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                showMoreMenuDialog = false
                                viewModel.navigateTo(Screen.BATTERY_GUIDANCE)
                            }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = BrandRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Battery & Background Settings", color = Color.Black, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
                    }

                    // Share App Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                showMoreMenuDialog = false
                                shareApp(context)
                            }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Share Get Taxi Meter", color = Color.Black, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
                    }

                    // Help & About
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                showMoreMenuDialog = false
                                viewModel.navigateTo(Screen.SETTINGS)
                            }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Tariff & App Info", color = Color.Black, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showMoreMenuDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed, contentColor = Color.White)
                ) {
                    Text("Close")
                }
            }
        )
    }
}

private fun shareApp(context: Context) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            "Get Taxi Meter - Safe Rides, Transparent Fares. Professional GPS Taxi Meter for Android."
        )
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Get Taxi Meter")
    context.startActivity(shareIntent)
}

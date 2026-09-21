package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.TripStatus
import com.example.service.TaxiMeterService
import com.example.ui.components.CompactMeterDashboard
import com.example.ui.theme.BrandRed
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MeterViewModel
import com.example.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MeterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recoveredTrip by viewModel.recoveredTrip.collectAsState()
    val todayTrips by viewModel.todayCompletedTrips.collectAsState()
    val currentTariff by viewModel.currentTariff.collectAsState()
    val tripState by viewModel.tripState.collectAsState()

    var showTodaySummaryDialog by remember { mutableStateOf(false) }
    var showMoreMenuDialog by remember { mutableStateOf(false) }
    var showRecoveredTripDialog by remember { mutableStateOf(recoveredTrip != null) }

    val todayEarnings = todayTrips.sumOf { it.currentFare }
    val todayDistanceKm = todayTrips.sumOf { it.totalDistanceMeters } / 1000.0

    // Dynamic current Date & Time
    val currentDate = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date())
    val currentTime = SimpleDateFormat("hh:mm a", Locale.US).format(Date())

    // Check GPS provider status
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    val isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: true

    // Permission launcher for Location & Notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineLocationGranted || coarseLocationGranted) {
            if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            viewModel.startTrip(context)
        }
    }

    val onStartTripClick = {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineLocationGranted || coarseLocationGranted) {
            if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            viewModel.startTrip(context)
        } else {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    val isTripActive = tripState.status == TripStatus.ACTIVE || tripState.status == TripStatus.WAITING

    CompactMeterDashboard(
        isTripActive = isTripActive,
        onActionClick = {
            if (isTripActive) {
                viewModel.navigateTo(Screen.LIVE_METER)
            } else {
                onStartTripClick()
            }
        },
        gpsReady = isGpsEnabled,
        gpsAccuracyMeters = if (tripState.gpsAccuracyMeters > 0) tripState.gpsAccuracyMeters else 4f,
        isMoving = tripState.isMoving,
        currentSpeedKmH = tripState.currentSpeedKmH,
        currentFare = if (isTripActive) tripState.breakdown.totalFare else currentTariff.baseFare,
        baseFare = currentTariff.baseFare,
        distanceFare = if (isTripActive) tripState.breakdown.distanceFare else 0.0,
        waitingFare = if (isTripActive) tripState.breakdown.waitingFare else 0.0,
        extraFare = if (isTripActive) tripState.breakdown.extraChargesTotal else 0.0,
        distanceKm = if (isTripActive) tripState.distanceKm else 0.0,
        ratePerKm = currentTariff.distanceRatePerKm,
        tripDurationFormatted = if (isTripActive) TaxiMeterService.formatDuration(tripState.tripDurationSeconds) else "00:00:00",
        waitingDurationFormatted = if (isTripActive) TaxiMeterService.formatDuration(tripState.waitingDurationSeconds) else "00:00:00",
        waitingRatePerMinute = currentTariff.waitingRatePerMinute,
        totalExtras = if (isTripActive) tripState.breakdown.extraChargesTotal else 0.0,
        dateText = currentDate,
        timeText = currentTime,
        onAddToll = {
            if (isTripActive) viewModel.addExtra(context, "Toll", 50.0) else onStartTripClick()
        },
        onAddParking = {
            if (isTripActive) viewModel.addExtra(context, "Parking", 30.0) else onStartTripClick()
        },
        onAddAirport = {
            if (isTripActive) viewModel.addExtra(context, "Airport", 100.0) else onStartTripClick()
        },
        onAddCustom = {
            if (isTripActive) viewModel.navigateTo(Screen.LIVE_METER) else onStartTripClick()
        },
        onMenuClick = { showMoreMenuDialog = true },
        onSettingsClick = { viewModel.navigateTo(Screen.SETTINGS) },
        onShareClick = { shareApp(context) },
        onTripHistoryClick = { viewModel.navigateTo(Screen.HISTORY) },
        onTariffSettingsClick = { viewModel.navigateTo(Screen.SETTINGS) },
        onTodaySummaryClick = { showTodaySummaryDialog = true },
        onMoreClick = { showMoreMenuDialog = true },
        modifier = modifier
    )

    // RECOVERED UNFINISHED TRIP DIALOG
    if (recoveredTrip != null && showRecoveredTripDialog) {
        val trip = recoveredTrip!!
        AlertDialog(
            onDismissRequest = { showRecoveredTripDialog = false },
            containerColor = Color.White,
            icon = {
                Icon(Icons.Default.Warning, contentDescription = null, tint = BrandRed, modifier = Modifier.size(32.dp))
            },
            title = {
                Text(
                    text = "Resume Unfinished Trip?",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "An active trip was in progress before the app closed. Fare accumulated: ₹${String.format(Locale.US, "%.2f", trip.currentFare)} (${String.format(Locale.US, "%.2f km", trip.totalDistanceMeters / 1000.0)}).",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRecoveredTripDialog = false
                        viewModel.resumeRecoveredTrip(context, trip.tripId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed, contentColor = Color.White)
                ) {
                    Text("Resume Trip", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showRecoveredTripDialog = false
                        viewModel.discardRecoveredTrip(trip)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Text("Discard")
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

                    // Help & Tariff Settings
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

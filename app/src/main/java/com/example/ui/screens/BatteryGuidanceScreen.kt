package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppCardBorder
import com.example.ui.theme.AppCardSecondary
import com.example.ui.theme.AppWhiteBg
import com.example.ui.theme.BrandRed
import com.example.ui.theme.MeterAmber
import com.example.ui.theme.MeterGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MeterViewModel
import com.example.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryGuidanceScreen(
    viewModel: MeterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val isIgnoringBatteryOptimizations = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        powerManager.isIgnoringBatteryOptimizations(context.packageName)
    } else true

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Battery & Background Guidance",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.HOME) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppWhiteBg)
            )
        },
        containerColor = AppWhiteBg
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AppCardBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = AppWhiteBg),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BatteryAlert,
                            contentDescription = "Battery Alert",
                            tint = BrandRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Continuous Meter Reliability",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "For reliable taxi meter operation when your phone screen is locked or while using other apps (Google Maps, WhatsApp), Android uses an active Foreground Service with ongoing notification.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    // Current Battery Status Pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppCardSecondary)
                            .border(1.dp, AppCardBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isIgnoringBatteryOptimizations) Icons.Default.CheckCircle else Icons.Default.BatteryAlert,
                            contentDescription = "Status",
                            tint = if (isIgnoringBatteryOptimizations) MeterGreen else MeterAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isIgnoringBatteryOptimizations) "Battery Optimizations: Disabled (Optimal)" else "Standard Battery Policy Active",
                                color = Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isIgnoringBatteryOptimizations) "Foreground location updates will run without interruption." else "May be paused on some devices after extended sleep.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // OEM Device Warnings
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AppCardBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = AppWhiteBg),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "MANUFACTURER SPECIFIC NOTES",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    GuidanceBullet(
                        title = "Samsung / Xiaomi / Vivo / Oppo",
                        description = "Set Get Taxi Meter battery usage to 'Unrestricted' and enable 'Autostart' if prompted by device manager."
                    )

                    GuidanceBullet(
                        title = "Screen Off Operation",
                        description = "The app automatically holds an internal WakeLock during active trips to calculate waiting and moving fares accurately."
                    )

                    GuidanceBullet(
                        title = "Trip Auto-Recovery",
                        description = "In case the system unexpectedly restarts the app, your active trip is persistently stored in Room database and automatically recovered."
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action to App Settings
            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed, contentColor = Color.White)
            ) {
                Icon(imageVector = Icons.Default.OpenInNew, contentDescription = "Settings", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Open App System Settings", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun GuidanceBullet(title: String, description: String) {
    Column {
        Text(text = "• $title", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text(
            text = description,
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            modifier = Modifier.padding(start = 12.dp, top = 2.dp)
        )
    }
}

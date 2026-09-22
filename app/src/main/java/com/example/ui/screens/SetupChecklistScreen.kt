package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.service.FloatingOverlayManager
import com.example.ui.components.BrandLogo
import com.example.ui.components.CurvedBrandFooter
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

@Composable
fun SetupChecklistScreen(
    viewModel: MeterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val driverProfile by viewModel.driverProfile.collectAsState()
    val isActivated by viewModel.isActivated.collectAsState()

    // Real-time Permission States
    var hasFineLocation by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasBackgroundLocation by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    var isBatteryOptimizedIgnored by remember {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isIgnored = pm?.isIgnoringBatteryOptimizations(context.packageName) ?: true
        mutableStateOf(isIgnored)
    }

    var canDrawOverlay by remember {
        mutableStateOf(FloatingOverlayManager.canDrawOverlay(context))
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    fun refreshChecks() {
        hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        hasBackgroundLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else true

        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        isBatteryOptimizedIgnored = pm?.isIgnoringBatteryOptimizations(context.packageName) ?: true
        canDrawOverlay = FloatingOverlayManager.canDrawOverlay(context)

        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    // Re-check state every time the driver returns to the app
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshChecks()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Permission launchers
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        refreshChecks()
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        refreshChecks()
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppWhiteBg)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(Screen.HOME) }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            BrandLogo(compact = true)
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Setup Checklist",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Text(
            text = "Ensure all permissions & activations are configured for taxi duty",
            fontSize = 12.5.sp,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Checklist Items Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Driver Profile
            ChecklistItemCard(
                icon = Icons.Default.Person,
                title = "1. Driver Profile",
                description = if (driverProfile.isRegistered) "Registered: ${driverProfile.name} (${driverProfile.vehicleNumber})" else "Driver name, mobile & vehicle details required",
                isCompleted = driverProfile.isRegistered,
                actionLabel = if (driverProfile.isRegistered) "EDIT" else "SET UP",
                onAction = { viewModel.navigateTo(Screen.DRIVER_PROFILE) }
            )

            // 2. Fine Location
            ChecklistItemCard(
                icon = Icons.Default.LocationOn,
                title = "2. GPS Location Permission",
                description = if (hasFineLocation) "Precise GPS tracking enabled" else "High-accuracy GPS required for fare calculation",
                isCompleted = hasFineLocation,
                actionLabel = "GRANT",
                onAction = {
                    locationLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            )

            // 3. Background Location ("Allow all the time")
            ChecklistItemCard(
                icon = Icons.Default.LocationOn,
                title = "3. Background Location",
                description = if (hasBackgroundLocation) "Continuous location in background enabled" else "Set location access to 'Allow all the time' in Settings",
                isCompleted = hasBackgroundLocation,
                actionLabel = "ALLOW ALL",
                onAction = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                        }
                    }
                }
            )

            // 4. Battery Optimization Exemption
            ChecklistItemCard(
                icon = Icons.Default.BatteryChargingFull,
                title = "4. Battery Optimization",
                description = if (isBatteryOptimizedIgnored) "Unrestricted background execution granted" else "Exclude Get Taxi Meter from power throttling for continuous tracking",
                isCompleted = isBatteryOptimizedIgnored,
                actionLabel = "UNRESTRICT",
                onAction = {
                    try {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        context.startActivity(fallback)
                    }
                }
            )

            // 5. Display Over Other Apps (Overlay)
            ChecklistItemCard(
                icon = Icons.Default.Layers,
                title = "5. Display Over Other Apps",
                description = if (canDrawOverlay) "Floating meter overlay enabled" else "Allows floating live fare widget while using navigation apps",
                isCompleted = canDrawOverlay,
                actionLabel = "ENABLE",
                onAction = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    }
                }
            )

            // 6. Notifications Permission
            ChecklistItemCard(
                icon = Icons.Default.Notifications,
                title = "6. Ongoing Trip Notification",
                description = if (hasNotificationPermission) "Foreground service notifications enabled" else "Displays live ongoing trip fare in system status bar",
                isCompleted = hasNotificationPermission,
                actionLabel = "ALLOW",
                onAction = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )

            // 7. Device Activation
            ChecklistItemCard(
                icon = Icons.Default.VpnKey,
                title = "7. Device Activation",
                description = if (isActivated) "Status: ACTIVE • Device bound successfully" else "Enter single-use administrator activation code",
                isCompleted = isActivated,
                actionLabel = if (isActivated) "VIEW" else "ACTIVATE",
                onAction = { viewModel.navigateTo(Screen.ACTIVATION) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Continue Button
        val allEssentialCompleted = driverProfile.isRegistered && hasFineLocation && isActivated

        Button(
            onClick = {
                if (allEssentialCompleted) {
                    viewModel.navigateTo(Screen.HOME)
                } else if (!driverProfile.isRegistered) {
                    viewModel.navigateTo(Screen.DRIVER_PROFILE)
                } else if (!isActivated) {
                    viewModel.navigateTo(Screen.ACTIVATION)
                } else {
                    viewModel.navigateTo(Screen.HOME)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (allEssentialCompleted) MeterGreen else BrandRed
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = if (allEssentialCompleted) "CONTINUE TO METER" else "COMPLETE REQUIRED SETUP",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        CurvedBrandFooter()
    }
}

@Composable
private fun ChecklistItemCard(
    icon: ImageVector,
    title: String,
    description: String,
    isCompleted: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) Color(0xFFF9FAFB) else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCompleted) Color(0xFFE5E7EB) else Color(0xFFFEE2E2)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Pill
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.Check else icon,
                    contentDescription = null,
                    tint = if (isCompleted) MeterGreen else BrandRed,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCompleted) "✓ COMPLETED" else "⚠ REQUIRED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) MeterGreen else BrandRed
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = description,
                    fontSize = 11.5.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = onAction,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isCompleted) AppCardBorder else BrandRed
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isCompleted) TextPrimary else BrandRed
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(
                    text = actionLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

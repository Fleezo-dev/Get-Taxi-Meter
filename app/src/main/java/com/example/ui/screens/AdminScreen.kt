package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppRole
import com.example.data.DeviceIdManager
import com.example.data.DeviceInstallation
import com.example.data.RideMode
import com.example.ui.components.BrandLogo
import com.example.ui.components.CurvedBrandFooter
import com.example.ui.theme.AppCardBorder
import com.example.ui.theme.AppCardSecondary
import com.example.ui.theme.AppWhiteBg
import com.example.ui.theme.BrandRed
import com.example.ui.theme.MeterGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MeterViewModel
import com.example.viewmodel.Screen
import kotlinx.coroutines.launch

@Composable
fun AdminScreen(
    viewModel: MeterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isAuthenticated by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    val driverProfile by viewModel.driverProfile.collectAsState()
    val isActivated by viewModel.isActivated.collectAsState()
    val localDeviceId = remember { DeviceIdManager.getDeviceId(context) }
    val isMasterAdmin = viewModel.hasRole(AppRole.MASTER_ADMIN)
    val installations by viewModel.installations.collectAsState()
    val installationsLoading by viewModel.installationsLoading.collectAsState()

    LaunchedEffect(isMasterAdmin, isAuthenticated) {
        if (isMasterAdmin && isAuthenticated) {
            viewModel.refreshInstallations()
        }
    }

    // Admin Generator State
    var targetDeviceId by remember { mutableStateOf(localDeviceId) }
    var generatedCode by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var showLoadTripDialog by remember { mutableStateOf(false) }
    var selectedInstallation by remember { mutableStateOf<DeviceInstallation?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppWhiteBg)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Top App Bar
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

        Spacer(modifier = Modifier.height(16.dp))

        if (!isMasterAdmin) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Admin authentication required",
                        tint = BrandRed,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Administrator Login Required",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Driver/test devices cannot access the admin activation generator.",
                        fontSize = 12.5.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = { viewModel.navigateTo(Screen.AUTH) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ADMINISTRATOR LOGIN", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (!isAuthenticated) {
            // PIN Authentication Screen
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFFFEE2E2), RoundedCornerShape(28.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Admin Lock",
                            tint = BrandRed,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Admin Authentication",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = "Enter administrator security PIN to continue",
                        fontSize = 12.5.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 8) {
                                pinInput = it
                                pinError = null
                            }
                        },
                        placeholder = { Text("Enter PIN", color = TextMuted) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandRed,
                            unfocusedBorderColor = AppCardBorder
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (pinError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = pinError!!,
                            color = BrandRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (pinInput == "140423") {
                                isAuthenticated = true
                                pinError = null
                            } else {
                                pinError = "Incorrect PIN. Access denied."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "UNLOCK ADMIN PANEL",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        } else {
            // Authenticated Admin Panel
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = BrandRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ADMIN PANEL",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. DEVICE / INSTALLATION MONITOR
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "DEVICE / INSTALLATION MONITOR", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = "See phones that have opened Get Taxi Meter", fontSize = 11.5.sp, color = TextSecondary, modifier = Modifier.padding(top = 3.dp))
                        }
                        Button(
                            onClick = { viewModel.refreshInstallations() },
                            enabled = !installationsLoading,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 7.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppCardSecondary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppCardBorder),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (installationsLoading) {
                                CircularProgressIndicator(color = BrandRed, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text("REFRESH", color = TextPrimary, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    when {
                        installationsLoading && installations.isEmpty() -> CircularProgressIndicator(color = BrandRed, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        installations.isEmpty() -> Text("No installations have reported yet.", fontSize = 12.sp, color = TextSecondary)
                        else -> installations.forEachIndexed { index, installation ->
                            InstallationMonitorRow(installation) { selectedInstallation = installation; showLoadTripDialog = true }
                            if (index < installations.lastIndex) HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = AppCardBorder)
                        }
                    }
                }
            }

            if (showLoadTripDialog && selectedInstallation != null) {
                LoadTripDialog(
                    installation = selectedInstallation!!,
                    viewModel = viewModel,
                    onDismiss = { showLoadTripDialog = false },
                    coroutineScope = coroutineScope
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            // 2. Generate Activation Code Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. GENERATE ACTIVATION CODE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Target Device ID",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = targetDeviceId,
                        onValueChange = { targetDeviceId = it.uppercase() },
                        placeholder = { Text("GTM-XXXX-XXXX-XXXX", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandRed,
                            unfocusedBorderColor = AppCardBorder
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (targetDeviceId.isNotBlank()) {
                                isGenerating = true
                                coroutineScope.launch {
                                    val (code, _) = viewModel.generateAdminActivationCode(targetDeviceId)
                                    generatedCode = code
                                    isGenerating = false
                                }
                            }
                        },
                        enabled = !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("GENERATE ACTIVATION CODE", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (generatedCode != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppCardSecondary, RoundedCornerShape(8.dp))
                                .border(1.dp, MeterGreen, RoundedCornerShape(8.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "DEVICE ID: $targetDeviceId",
                                    fontSize = 11.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "ACTIVATION CODE: $generatedCode",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = BrandRed
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        DeviceIdManager.copyToClipboard(context, generatedCode!!)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AppCardBorder),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("COPY ACTIVATION CODE", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Driver Details & Status Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. CURRENT DEVICE & DRIVER DETAILS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AdminDetailRow(label = "Device ID", value = localDeviceId)
                    AdminDetailRow(label = "Activation Status", value = if (isActivated) "ACTIVE" else "NOT ACTIVATED", isHighlight = isActivated)
                    AdminDetailRow(label = "Driver Name", value = driverProfile.name.ifBlank { "Not Registered" })
                    AdminDetailRow(label = "Mobile Number", value = driverProfile.mobileNumber.ifBlank { "—" })
                    AdminDetailRow(label = "Vehicle Number", value = driverProfile.vehicleNumber.ifBlank { "—" })
                    AdminDetailRow(label = "Vehicle Type", value = driverProfile.vehicleType)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Quick Actions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "4. QUICK ACTIONS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.navigateTo(Screen.ACTIVATION) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AppCardSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppCardBorder),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("OPEN DEVICE ACTIVATION SCREEN", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.navigateTo(Screen.DRIVER_PROFILE) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AppCardSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppCardBorder),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("EDIT DRIVER PROFILE", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.navigateTo(Screen.SETUP_CHECKLIST) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AppCardSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppCardBorder),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("VIEW SETUP CHECKLIST", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { isAuthenticated = false; viewModel.navigateTo(Screen.HOME) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2937)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("LOCK & RETURN TO METER", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        CurvedBrandFooter()
    }
}

@Composable
private fun InstallationMonitorRow(installation: DeviceInstallation, onLoadTrip: () -> Unit) {
    val statusText = when (installation.activationStatus.uppercase()) {
        "ACTIVE" -> "ACTIVE"
        "PENDING" -> "PENDING"
        else -> "NOT ACTIVATED"
    }
    val statusColor = if (statusText == "ACTIVE") MeterGreen else BrandRed

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = installation.deviceId, fontFamily = FontFamily.Monospace, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = statusText, color = statusColor, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(5.dp))
        val driver = installation.driverName.ifBlank { "Driver not registered" }
        val vehicle = installation.vehicleNumber.ifBlank { "Vehicle not registered" }
        Text(text = "$driver  •  $vehicle", fontSize = 11.5.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = "First seen: ${installation.firstSeenAt?.let { formatInstallationTime(it) } ?: "Waiting for sync"}", fontSize = 10.5.sp, color = TextSecondary)
        Text(text = "Last seen: " + (installation.lastSeenAt?.let { formatInstallationTime(it) } ?: "Waiting for sync") + "  •  v" + installation.appVersion.ifBlank { "—" }, fontSize = 10.5.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onLoadTrip,
            enabled = installation.activationStatus.equals("ACTIVE", ignoreCase = true) && installation.ownerUid.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(38.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("LOAD TRIP TO THIS DRIVER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatInstallationTime(timeMs: Long): String =
    java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(timeMs))

@Composable
private fun LoadTripDialog(
    installation: DeviceInstallation,
    viewModel: MeterViewModel,
    onDismiss: () -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    var tripReference by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var customerMobile by remember { mutableStateOf("") }
    var pickup by remember { mutableStateOf("") }
    var drop by remember { mutableStateOf("") }
    var rideMode by remember { mutableStateOf(RideMode.CITY_RIDE) }
    var saving by remember { mutableStateOf(false) }
    var generatedOtp by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        containerColor = Color.White,
        title = { Text("LOAD TRIP TO DRIVER", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Driver: " + installation.driverName.ifBlank { installation.deviceId }, fontSize = 12.sp, color = TextSecondary)
                Text("The generated OTP is given to the driver. The driver enters it in Load Trip on the meter.", fontSize = 11.sp, color = TextSecondary)
                OutlinedTextField(value = tripReference, onValueChange = { tripReference = it }, label = { Text("Trip Reference") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = customerName, onValueChange = { customerName = it }, label = { Text("Customer Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = customerMobile, onValueChange = { customerMobile = it }, label = { Text("Customer Mobile") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = pickup, onValueChange = { pickup = it }, label = { Text("Pickup") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = drop, onValueChange = { drop = it }, label = { Text("Drop") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("Ride Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    RideMode.entries.forEach { mode ->
                        Button(
                            onClick = { rideMode = mode },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = if (rideMode == mode) BrandRed else AppCardSecondary),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 7.dp)
                        ) {
                            Text(mode.name.replace('_', ' '), fontSize = 9.sp, color = if (rideMode == mode) Color.White else TextPrimary)
                        }
                    }
                }
                if (error != null) Text(error!!, color = BrandRed, fontSize = 11.sp)
                if (generatedOtp != null) {
                    Box(modifier = Modifier.fillMaxWidth().background(AppCardSecondary, RoundedCornerShape(10.dp)).border(1.dp, MeterGreen, RoundedCornerShape(10.dp)).padding(12.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("DRIVER LOAD OTP", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(generatedOtp!!, color = BrandRed, fontSize = 28.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                            Text("Give this OTP to the driver. It can be used once.", color = TextSecondary, fontSize = 10.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (generatedOtp == null) {
                Button(
                    onClick = {
                        if (pickup.isBlank() || drop.isBlank()) { error = "Pickup and Drop are required"; return@Button }
                        saving = true
                        error = null
                        coroutineScope.launch {
                            viewModel.createTripAssignment(
                                deviceId = installation.deviceId,
                                ownerUid = installation.ownerUid,
                                tripReference = tripReference,
                                customerName = customerName,
                                customerMobile = customerMobile,
                                pickup = pickup,
                                drop = drop,
                                rideMode = rideMode
                            ).onSuccess { generatedOtp = it.second }
                                .onFailure { error = it.message ?: "Unable to load trip" }
                            saving = false
                        }
                    },
                    enabled = !saving,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                ) {
                    if (saving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Text("GENERATE LOAD OTP")
                }
            } else {
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = BrandRed)) { Text("DONE") }
            }
        },
        dismissButton = {
            if (generatedOtp == null) {
                androidx.compose.material3.TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancel") }
            }
        }
    )
}

@Composable
private fun AdminDetailRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
        Text(
            text = value,
            color = if (isHighlight) MeterGreen else TextPrimary,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

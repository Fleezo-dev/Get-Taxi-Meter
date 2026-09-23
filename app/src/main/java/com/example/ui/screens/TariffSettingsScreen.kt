package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RidePricing
import com.example.ui.theme.AppCardBorder
import com.example.ui.theme.AppWhiteBg
import com.example.ui.theme.BrandRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MeterViewModel
import com.example.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TariffSettingsScreen(
    viewModel: MeterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTariff by viewModel.currentTariff.collectAsState()

    var tariffName by remember(currentTariff) { mutableStateOf(currentTariff.name) }
    var baseFareText by remember(currentTariff) { mutableStateOf(currentTariff.baseFare.toString()) }
    var distanceRateText by remember(currentTariff) { mutableStateOf(currentTariff.distanceRatePerKm.toString()) }
    var distanceChargeEnabled by remember(currentTariff) { mutableStateOf(currentTariff.distanceChargeEnabled) }
    var waitingRateText by remember(currentTariff) { mutableStateOf(currentTariff.waitingRatePerMinute.toString()) }
    var freeDistanceText by remember(currentTariff) { mutableStateOf(currentTariff.freeDistanceKm.toString()) }
    var speedThresholdText by remember(currentTariff) { mutableStateOf(currentTariff.waitingSpeedThresholdKmH.toString()) }

    val ridePricing by viewModel.ridePricing.collectAsState()
    var hourlyRateText by remember(ridePricing) { mutableStateOf(ridePricing.hourlyRate.toString()) }
    var hourlyFreeKmText by remember(ridePricing) { mutableStateOf(ridePricing.hourlyFreeKm.toString()) }
    var hourlyExtraKmRateText by remember(ridePricing) { mutableStateOf(ridePricing.hourlyExtraKmRate.toString()) }
    var outstationDriverBataText by remember(ridePricing) { mutableStateOf(ridePricing.outstationDriverBata.toString()) }
    var outstationPerKmRateText by remember(ridePricing) { mutableStateOf(ridePricing.outstationPerKmRate.toString()) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tariff / Rate Settings",
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "OPERATOR CONFIGURATION",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    TariffInputField(
                        label = "Tariff Name",
                        value = tariffName,
                        onValueChange = { tariffName = it },
                        keyboardType = KeyboardType.Text
                    )

                    TariffInputField(
                        label = "Base Fare (₹)",
                        value = baseFareText,
                        onValueChange = { baseFareText = it }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TariffInputField(
                            label = "Distance Rate (₹/km)",
                            value = distanceRateText,
                            onValueChange = { distanceRateText = it },
                            modifier = Modifier.weight(1f)
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Charge Per KM",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Switch(
                                checked = distanceChargeEnabled,
                                onCheckedChange = { distanceChargeEnabled = it }
                            )
                        }
                    }

                    TariffInputField(
                        label = "Waiting Rate (₹/min)",
                        value = waitingRateText,
                        onValueChange = { waitingRateText = it }
                    )

                    TariffInputField(
                        label = "Free Distance (km)",
                        value = freeDistanceText,
                        onValueChange = { freeDistanceText = it }
                    )

                    Text(
                        text = "Waiting has no free minutes. The waiting rate applies from the first chargeable minute.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )

                    TariffInputField(
                        label = "Waiting Speed Threshold (km/h)",
                        value = speedThresholdText,
                        onValueChange = { speedThresholdText = it },
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "OUTSTATION",
                        color = BrandRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TariffInputField(
                            label = "Driver Bata / Base Fare (₹)",
                            value = outstationDriverBataText,
                            onValueChange = { outstationDriverBataText = it },
                            modifier = Modifier.weight(1f)
                        )
                        TariffInputField(
                            label = "Per Km (₹/km)",
                            value = outstationPerKmRateText,
                            onValueChange = { outstationPerKmRateText = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(
                        text = "City Ride uses the normal tariff above. Drivers choose City Ride, Hourly Rental, or Outstation before starting each trip.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val base = baseFareText.toDoubleOrNull() ?: 80.0
                    val distRate = distanceRateText.toDoubleOrNull() ?: 28.0
                    val waitRate = waitingRateText.toDoubleOrNull() ?: 2.0
                    val freeDist = freeDistanceText.toDoubleOrNull() ?: 0.0
                    val speedThreshold = speedThresholdText.toDoubleOrNull() ?: 5.0

                    val updatedTariff = currentTariff.copy(
                        name = if (tariffName.isBlank()) "Standard City Tariff" else tariffName.trim(),
                        baseFare = base,
                        distanceRatePerKm = distRate,
                        waitingRatePerMinute = waitRate,
                        freeDistanceKm = freeDist,
                        waitingSpeedThresholdKmH = speedThreshold,
                        freeWaitingMinutes = 0.0,
                        distanceChargeEnabled = distanceChargeEnabled
                    )

                    viewModel.saveTariff(updatedTariff)
                    viewModel.saveRidePricing(
                        RidePricing(
                            hourlyRate = hourlyRateText.toDoubleOrNull() ?: 350.0,
                            hourlyFreeKm = hourlyFreeKmText.toDoubleOrNull() ?: 10.0,
                            hourlyExtraKmRate = hourlyExtraKmRateText.toDoubleOrNull() ?: 20.0,
                            outstationDriverBata = outstationDriverBataText.toDoubleOrNull() ?: 500.0,
                            outstationPerKmRate = outstationPerKmRateText.toDoubleOrNull() ?: 30.0
                        )
                    )
                    Toast.makeText(context, "All tariff settings saved successfully", Toast.LENGTH_SHORT).show()
                    viewModel.navigateTo(Screen.HOME)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed, contentColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "SAVE TARIFF SETTINGS", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TariffInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Number
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedBorderColor = BrandRed,
            unfocusedBorderColor = AppCardBorder,
            focusedLabelColor = BrandRed,
            unfocusedLabelColor = TextSecondary
        )
    )
}

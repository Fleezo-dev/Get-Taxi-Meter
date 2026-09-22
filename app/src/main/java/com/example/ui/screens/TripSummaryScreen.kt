package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TripEntity
import com.example.service.TripInvoiceManager
import com.example.service.TaxiMeterService
import com.example.ui.components.BrandLogo
import com.example.ui.components.CurvedBrandFooter
import com.example.ui.theme.AppCardBorder
import com.example.ui.theme.AppCardBorderRed
import com.example.ui.theme.AppCardSecondary
import com.example.ui.theme.AppWhiteBg
import com.example.ui.theme.BrandRed
import com.example.ui.theme.MeterGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MeterViewModel
import com.example.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TripSummaryScreen(
    viewModel: MeterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tripState by viewModel.tripState.collectAsState()
    val selectedHistoryTrip by viewModel.selectedHistoryTrip.collectAsState()
    val driverProfile by viewModel.driverProfile.collectAsState()
    var pdfUri by remember { mutableStateOf<android.net.Uri?>(null) }

    // Either inspect a past completed trip or the just-completed trip
    val isHistoryMode = selectedHistoryTrip != null
    val targetState = if (isHistoryMode) selectedHistoryTrip!!.toTripState() else tripState

    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.US)
    val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.US)

    val startDateStr = dateFormat.format(Date(if (targetState.startTimestamp > 0) targetState.startTimestamp else System.currentTimeMillis()))
    val startTimeStr = timeFormat.format(Date(if (targetState.startTimestamp > 0) targetState.startTimestamp else System.currentTimeMillis()))
    val endTimeStr = if (targetState.endTimestamp != null && targetState.endTimestamp > 0) {
        timeFormat.format(Date(targetState.endTimestamp))
    } else {
        timeFormat.format(Date(System.currentTimeMillis()))
    }

    val receiptText = buildString {
        appendLine("===============================")
        appendLine("       GET TAXI METER          ")
        appendLine("       TRIP RECEIPT            ")
        appendLine("===============================")
        appendLine("Date: $startDateStr")
        appendLine("Start: $startTimeStr")
        appendLine("End:   $endTimeStr")
        appendLine("Distance: ${String.format(Locale.US, "%.2f km", targetState.distanceKm)}")
        appendLine("Duration: ${TaxiMeterService.formatDuration(targetState.tripDurationSeconds)}")
        appendLine("Waiting:  ${TaxiMeterService.formatDuration(targetState.waitingDurationSeconds)}")
        appendLine("-------------------------------")
        appendLine("Base Fare:     ₹${String.format(Locale.US, "%.2f", targetState.breakdown.baseFare)}")
        appendLine("Distance Fare: ₹${String.format(Locale.US, "%.2f", targetState.breakdown.distanceFare)}")
        appendLine("Waiting Fare:  ₹${String.format(Locale.US, "%.2f", targetState.breakdown.waitingFare)}")
        if (targetState.breakdown.extraChargesTotal > 0) {
            appendLine("Extra Charges: ₹${String.format(Locale.US, "%.2f", targetState.breakdown.extraChargesTotal)}")
        }
        appendLine("===============================")
        appendLine("TOTAL FARE:    ₹${String.format(Locale.US, "%.2f", targetState.breakdown.totalFare)}")
        appendLine("===============================")
        appendLine("SAFE RIDES • TRANSPARENT FARES")
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

        BrandLogo(compact = true)

        Spacer(modifier = Modifier.height(12.dp))

        // Success Pill
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFDCFCE7))
                .border(1.dp, MeterGreen, RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Completed",
                tint = MeterGreen,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isHistoryMode) "COMPLETED TRIP RECORD" else "TRIP COMPLETED",
                color = MeterGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Receipt Card (Clean white card with subtle border & red accent)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.5.dp, AppCardBorderRed, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = AppWhiteBg),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "RECEIPT SUMMARY",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Date", color = TextSecondary, fontSize = 12.sp)
                    Text(text = startDateStr, color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Time", color = TextSecondary, fontSize = 12.sp)
                    Text(text = "$startTimeStr → $endTimeStr", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = AppCardBorder)
                Spacer(modifier = Modifier.height(12.dp))

                // Trip Telemetry
                ReceiptRow(label = "Total Distance", value = String.format(Locale.US, "%.2f km", targetState.distanceKm))
                ReceiptRow(label = "Trip Time", value = TaxiMeterService.formatDuration(targetState.tripDurationSeconds))
                ReceiptRow(label = "Waiting Time", value = TaxiMeterService.formatDuration(targetState.waitingDurationSeconds))

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = AppCardBorder)
                Spacer(modifier = Modifier.height(12.dp))

                // Fare breakdown
                ReceiptRow(
                    label = "Base Fare",
                    value = String.format(Locale.US, "₹%.2f", targetState.breakdown.baseFare)
                )
                ReceiptRow(
                    label = "Distance Fare (${String.format(Locale.US, "%.2f km", targetState.breakdown.chargeableDistanceKm)})",
                    value = String.format(Locale.US, "₹%.2f", targetState.breakdown.distanceFare)
                )
                ReceiptRow(
                    label = "Waiting Fare (${String.format(Locale.US, "%.1f min", targetState.breakdown.chargeableWaitingMinutes)})",
                    value = String.format(Locale.US, "₹%.2f", targetState.breakdown.waitingFare)
                )

                if (targetState.breakdown.extraChargesTotal > 0) {
                    ReceiptRow(
                        label = "Extra Charges",
                        value = String.format(Locale.US, "₹%.2f", targetState.breakdown.extraChargesTotal),
                        valueColor = BrandRed
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = AppCardBorder, thickness = 1.5.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Total Fare Highlight
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL FARE",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = String.format(Locale.US, "₹%.2f", targetState.breakdown.totalFare),
                        color = BrandRed,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Premium PDF actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    pdfUri = TripInvoiceManager.generatePdf(context, targetState, driverProfile, viewModel.getPaymentQrPath())
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed, contentColor = Color.White)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save PDF", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = {
                    val uri = pdfUri ?: TripInvoiceManager.generatePdf(context, targetState, driverProfile, viewModel.getPaymentQrPath())
                    pdfUri = uri
                    if (uri != null) TripInvoiceManager.sharePdf(context, uri)
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandRed)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share PDF", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Share Receipt
            OutlinedButton(
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, receiptText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Trip Receipt"))
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp), tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Share Receipt", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            // View History
            OutlinedButton(
                onClick = { viewModel.navigateTo(Screen.HISTORY) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
            ) {
                Icon(imageVector = Icons.Default.History, contentDescription = "History", modifier = Modifier.size(18.dp), tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Trip History", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Start New Trip / Return to Home
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Button(
                onClick = { viewModel.startNewTripFromSummary() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed, contentColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                Icon(imageVector = Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "START NEW TRIP", fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Curved Brand Footer
        CurvedBrandFooter()
    }
}

@Composable
fun ReceiptRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 13.sp)
        Text(
            text = value,
            color = valueColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.model.TripStatus
import com.example.ui.screens.ActivationScreen
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BatteryGuidanceScreen
import com.example.ui.screens.DriverProfileScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveMeterScreen
import com.example.ui.screens.SetupChecklistScreen
import com.example.ui.screens.TariffSettingsScreen
import com.example.ui.screens.TripHistoryScreen
import com.example.ui.screens.TripSummaryScreen
import com.example.ui.theme.GetTaxiMeterTheme
import com.example.ui.theme.MeterBlackBg
import com.example.viewmodel.MeterViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private val viewModel: MeterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GetTaxiMeterTheme {
                MainContent(viewModel = viewModel, onMinimizeApp = { moveTaskToBack(true) })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check for any ongoing active trip or recovered trip on resume
        viewModel.checkForUnfinishedTrip()
    }
}

@Composable
fun MainContent(
    viewModel: MeterViewModel,
    onMinimizeApp: () -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val tripState by viewModel.tripState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var lastBackPressAt by remember { mutableStateOf(0L) }

    // Safe Back handling: Leaving Live Meter screen does NOT kill the trip!
    BackHandler {
        if (currentScreen == Screen.AUTH) return@BackHandler
        if (!viewModel.isActivated.value && currentScreen != Screen.ACTIVATION && currentScreen != Screen.DRIVER_PROFILE) {
            Toast.makeText(context, "Activation is required to use the meter", Toast.LENGTH_SHORT).show()
            return@BackHandler
        }
        when (currentScreen) {
            Screen.AUTH -> Unit
            Screen.LIVE_METER -> {
                if (tripState.status == TripStatus.ACTIVE || tripState.status == TripStatus.WAITING) {
                    // Minimize app so trip continues safely in Foreground Service
                    onMinimizeApp()
                } else {
                    viewModel.navigateTo(Screen.HOME)
                }
            }
            Screen.ACTIVATION -> {
                if (viewModel.isActivated.value) viewModel.navigateTo(Screen.HOME)
                else Toast.makeText(context, "Activation is required to use the meter", Toast.LENGTH_SHORT).show()
            }
            Screen.SUMMARY, Screen.HISTORY, Screen.SETTINGS, Screen.BATTERY_GUIDANCE,
            Screen.SETUP_CHECKLIST, Screen.DRIVER_PROFILE, Screen.ADMIN -> {
                viewModel.navigateTo(Screen.HOME)
            }
            Screen.HOME -> {
                val now = System.currentTimeMillis()
                if (now - lastBackPressAt < 1800L) {
                    (context as? MainActivity)?.finishAndRemoveTask()
                } else {
                    lastBackPressAt = now
                    Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = com.example.ui.theme.AppWhiteBg,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        when (currentScreen) {
            Screen.AUTH -> AuthScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            Screen.HOME -> HomeScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            Screen.LIVE_METER -> LiveMeterScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            Screen.SUMMARY -> TripSummaryScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            Screen.HISTORY -> TripHistoryScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            Screen.SETTINGS -> TariffSettingsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            Screen.BATTERY_GUIDANCE -> BatteryGuidanceScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            Screen.DRIVER_PROFILE -> DriverProfileScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            Screen.SETUP_CHECKLIST -> SetupChecklistScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            Screen.ACTIVATION -> ActivationScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            Screen.ADMIN -> AdminScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

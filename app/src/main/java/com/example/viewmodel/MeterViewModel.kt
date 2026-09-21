package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.TaxiMeterApplication
import com.example.data.TripEntity
import com.example.model.Tariff
import com.example.model.TripState
import com.example.model.TripStatus
import com.example.service.TaxiMeterService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class Screen {
    HOME,
    LIVE_METER,
    SUMMARY,
    HISTORY,
    SETTINGS,
    BATTERY_GUIDANCE
}

class MeterViewModel(application: Application) : AndroidViewModel(application) {

    private val db = TaxiMeterApplication.instance.database
    private val tariffRepo = TaxiMeterApplication.instance.tariffRepository

    val tripState: StateFlow<TripState> = TaxiMeterService.tripState
    val isServiceRunning: StateFlow<Boolean> = TaxiMeterService.isServiceRunning
    val currentTariff: StateFlow<Tariff> = tariffRepo.currentTariff

    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _recoveredTrip = MutableStateFlow<TripEntity?>(null)
    val recoveredTrip: StateFlow<TripEntity?> = _recoveredTrip.asStateFlow()

    private val _selectedHistoryTrip = MutableStateFlow<TripEntity?>(null)
    val selectedHistoryTrip: StateFlow<TripEntity?> = _selectedHistoryTrip.asStateFlow()

    private val startOfDayMs: Long
        get() {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }

    val todayCompletedTrips: StateFlow<List<TripEntity>> =
        db.tripDao().getTodayCompletedTrips(startOfDayMs)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCompletedTrips: StateFlow<List<TripEntity>> =
        db.tripDao().getAllCompletedTrips()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        checkForUnfinishedTrip()
    }

    fun checkForUnfinishedTrip() {
        viewModelScope.launch(Dispatchers.IO) {
            // If service is already running, switch to LIVE_METER automatically
            if (isServiceRunning.value || tripState.value.status == TripStatus.ACTIVE || tripState.value.status == TripStatus.WAITING) {
                _currentScreen.value = Screen.LIVE_METER
                return@launch
            }

            val unfinished = db.tripDao().getActiveTrip()
            if (unfinished != null) {
                _recoveredTrip.value = unfinished
            }
        }
    }

    fun startTrip(context: Context) {
        TaxiMeterService.startTrip(context)
        _currentScreen.value = Screen.LIVE_METER
    }

    fun resumeRecoveredTrip(context: Context, tripId: Long) {
        TaxiMeterService.resumeTrip(context, tripId)
        _recoveredTrip.value = null
        _currentScreen.value = Screen.LIVE_METER
    }

    fun discardRecoveredTrip(entity: TripEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = entity.copy(status = TripStatus.CANCELLED.name)
            db.tripDao().updateTrip(updated)
            _recoveredTrip.value = null
        }
    }

    fun addExtra(context: Context, label: String, amount: Double) {
        TaxiMeterService.addExtra(context, label, amount)
    }

    fun removeExtra(context: Context, extraId: String) {
        TaxiMeterService.removeExtra(context, extraId)
    }

    fun endTrip(context: Context) {
        TaxiMeterService.endTrip(context)
        _currentScreen.value = Screen.SUMMARY
    }

    fun startNewTripFromSummary() {
        TaxiMeterService.resetReadyState()
        _currentScreen.value = Screen.HOME
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun viewHistoryDetail(trip: TripEntity) {
        _selectedHistoryTrip.value = trip
        _currentScreen.value = Screen.SUMMARY
    }

    fun saveTariff(tariff: Tariff) {
        tariffRepo.saveTariff(tariff)
    }
}

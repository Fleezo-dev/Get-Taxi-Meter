package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.TaxiMeterApplication
import com.example.data.ActivationRepository
import com.example.data.ActivationResult
import com.example.data.AppRole
import com.example.data.AuthProfile
import com.example.data.AuthRepository
import com.example.data.DeviceActivationRecord
import com.example.data.DeviceIdManager
import com.example.data.DriverProfile
import com.example.data.DriverProfileRepository
import com.example.data.TripEntity
import com.example.data.RideMode
import com.example.data.RidePricing
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
    AUTH,
    HOME,
    LIVE_METER,
    SUMMARY,
    HISTORY,
    SETTINGS,
    BATTERY_GUIDANCE,
    SETUP_CHECKLIST,
    DRIVER_PROFILE,
    ACTIVATION,
    ADMIN
}

class MeterViewModel(application: Application) : AndroidViewModel(application) {

    private val db = TaxiMeterApplication.instance.database
    private val tariffRepo = TaxiMeterApplication.instance.tariffRepository
    private val driverRepo = TaxiMeterApplication.instance.driverProfileRepository
    private val activationRepo = TaxiMeterApplication.instance.activationRepository
    private val rideModeRepo = TaxiMeterApplication.instance.rideModeRepository
    private val paymentRepo = TaxiMeterApplication.instance.driverPaymentRepository
    private val authRepo = AuthRepository()

    private val _authProfile = MutableStateFlow<AuthProfile?>(null)
    val authProfile: StateFlow<AuthProfile?> = _authProfile.asStateFlow()
    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    val tripState: StateFlow<TripState> = TaxiMeterService.tripState
    val isServiceRunning: StateFlow<Boolean> = TaxiMeterService.isServiceRunning
    val currentTariff: StateFlow<Tariff> = tariffRepo.currentTariff

    val driverProfile: StateFlow<DriverProfile> = driverRepo.profile
    val isActivated: StateFlow<Boolean> = activationRepo.isActivated
    val activationStatus: StateFlow<String> = activationRepo.activationStatus

    val selectedRideMode: StateFlow<RideMode> = rideModeRepo.selectedMode
    val ridePricing: StateFlow<RidePricing> = rideModeRepo.pricing
    val driverPaymentQrPath: StateFlow<String?> = kotlinx.coroutines.flow.MutableStateFlow(paymentRepo.getQrPath())

    private val _currentScreen = MutableStateFlow(
        if (authRepo.isSignedIn()) nextAuthenticatedScreen() else Screen.AUTH
    )
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
        viewModelScope.launch { initializeAuth() }
        checkForUnfinishedTrip()
    }

    private suspend fun initializeAuth() {
        if (!authRepo.isSignedIn()) {
            _currentScreen.value = Screen.AUTH
            return
        }
        _authLoading.value = true
        when (val result = authRepo.loadOrBootstrapProfile()) {
            is com.example.data.AuthResult.Success -> {
                _authProfile.value = result.profile
                _authError.value = null
                _currentScreen.value = nextAuthenticatedScreen()
            }
            is com.example.data.AuthResult.Error -> {
                _authProfile.value = null
                _authError.value = result.message
                _currentScreen.value = Screen.AUTH
            }
        }
        _authLoading.value = false
    }

    private fun nextAuthenticatedScreen(): Screen {
        return if (!driverRepo.profile.value.isRegistered) Screen.DRIVER_PROFILE
        else if (!activationRepo.isActivated.value) Screen.ACTIVATION
        else Screen.HOME
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            when (val result = authRepo.signIn(email, password)) {
                is com.example.data.AuthResult.Success -> {
                    _authProfile.value = result.profile
                    _currentScreen.value = nextAuthenticatedScreen()
                }
                is com.example.data.AuthResult.Error -> {
                    _authProfile.value = null
                    _authError.value = result.message
                }
            }
            _authLoading.value = false
        }
    }

    fun signOut() {
        authRepo.signOut()
        _authProfile.value = null
        _currentScreen.value = Screen.AUTH
    }

    fun clearAuthError() { _authError.value = null }

    fun hasRole(role: AppRole): Boolean = _authProfile.value?.role == role

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

    fun setRideMode(mode: RideMode) {
        rideModeRepo.setMode(mode)
    }

    fun saveRidePricing(pricing: RidePricing) {
        rideModeRepo.savePricing(pricing)
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

    fun saveDriverProfile(
        name: String,
        mobileNumber: String,
        vehicleNumber: String,
        vehicleType: String,
        photoPath: String?
    ): Boolean {
        val success = driverRepo.saveProfile(name, mobileNumber, vehicleNumber, vehicleType, photoPath)
        if (success) {
            if (!isActivated.value) {
                _currentScreen.value = Screen.ACTIVATION
            } else {
                _currentScreen.value = Screen.HOME
            }
        }
        return success
    }

    fun saveProfilePhoto(uri: android.net.Uri): String? {
        return driverRepo.saveImageToInternalStorage(uri)
    }

    fun savePaymentQr(uri: android.net.Uri): String? {
        val path = driverRepo.saveImageToInternalStorage(uri)?.let { source ->
            val target = java.io.File(getApplication<Application>().filesDir, "driver_payment_qr.png")
            java.io.File(source).copyTo(target, overwrite = true)
            target.absolutePath
        }
        if (path != null) paymentRepo.saveQrPath(path)
        return path
    }

    fun getPaymentQrPath(): String? = paymentRepo.getQrPath()

    suspend fun activateDevice(activationCode: String): ActivationResult {
        val context = getApplication<Application>()
        val deviceId = DeviceIdManager.getDeviceId(context)
        val profile = driverProfile.value
        val result = activationRepo.activateDevice(deviceId, activationCode, profile)
        if (result is ActivationResult.Success) {
            _currentScreen.value = Screen.HOME
        }
        return result
    }

    suspend fun generateAdminActivationCode(deviceId: String): Pair<String, String?> {
        return activationRepo.generateActivationCodeForDevice(deviceId)
    }

    fun getDeviceRecord(deviceId: String): DeviceActivationRecord? {
        return activationRepo.getDeviceRecordLocally(deviceId)
    }
}

package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ble.BipMaxBleManager
import com.example.data.local.BipDatabase
import com.example.data.model.AppNotificationRule
import com.example.data.model.BipDevice
import com.example.data.model.ConnectionState
import com.example.data.model.CustomWatchFaceConfig
import com.example.data.model.HealthMetricRecord
import com.example.data.model.VibrationPattern
import com.example.data.model.WatchFace
import com.example.data.model.WatchSettings
import com.example.data.model.WorkoutRecord
import com.example.data.model.WorkoutType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BipMaxViewModel(application: Application) : AndroidViewModel(application) {

    private val db = BipDatabase.getInstance(application)
    private val dao = db.bipDao()
    val bleManager = BipMaxBleManager(application)

    val activeDevice: StateFlow<BipDevice?> = dao.getActiveDeviceFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val connectionState: StateFlow<ConnectionState> = bleManager.connectionState
    val discoveredDevices: StateFlow<List<BipDevice>> = bleManager.discoveredDevices
    val liveHeartRate: StateFlow<Int> = bleManager.liveHeartRate
    val liveSteps: StateFlow<Int> = bleManager.liveSteps
    val liveBattery: StateFlow<Int> = bleManager.liveBattery
    val isCharging: StateFlow<Boolean> = bleManager.isCharging
    val watchFaceProgress: StateFlow<Float?> = bleManager.watchFaceProgress

    val latestHealth: StateFlow<HealthMetricRecord?> = dao.getLatestHealthMetricFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentHealthList: StateFlow<List<HealthMetricRecord>> = dao.getRecentHealthMetricsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workouts: StateFlow<List<WorkoutRecord>> = dao.getAllWorkoutsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchFaces: StateFlow<List<WatchFace>> = dao.getAllWatchFacesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notificationRules: StateFlow<List<AppNotificationRule>> = dao.getAllNotificationRulesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _watchSettings = MutableStateFlow(WatchSettings())
    val watchSettings: StateFlow<WatchSettings> = _watchSettings.asStateFlow()

    private val _snackBarMessage = MutableSharedFlow<String>()
    val snackBarMessage: SharedFlow<String> = _snackBarMessage.asSharedFlow()

    // Live Workout Tracking State
    private val _isWorkoutActive = MutableStateFlow(false)
    val isWorkoutActive: StateFlow<Boolean> = _isWorkoutActive.asStateFlow()

    private val _isWorkoutPaused = MutableStateFlow(false)
    val isWorkoutPaused: StateFlow<Boolean> = _isWorkoutPaused.asStateFlow()

    private val _activeWorkoutType = MutableStateFlow(WorkoutType.OUTDOOR_RUN)
    val activeWorkoutType: StateFlow<WorkoutType> = _activeWorkoutType.asStateFlow()

    private val _activeWorkoutSeconds = MutableStateFlow(0L)
    val activeWorkoutSeconds: StateFlow<Long> = _activeWorkoutSeconds.asStateFlow()

    private val _activeWorkoutDistanceKm = MutableStateFlow(0.0f)
    val activeWorkoutDistanceKm: StateFlow<Float> = _activeWorkoutDistanceKm.asStateFlow()

    private val _activeWorkoutCalories = MutableStateFlow(0)
    val activeWorkoutCalories: StateFlow<Int> = _activeWorkoutCalories.asStateFlow()

    private var workoutJob: Job? = null

    init {
        viewModelScope.launch {
            // Keep active device battery synced with live telemetry
            combine(liveBattery, isCharging) { battery, charging ->
                Pair(battery, charging)
            }.collect { (battery, charging) ->
                activeDevice.value?.let { dev ->
                    dao.updateBattery(dev.macAddress, battery, charging, System.currentTimeMillis())
                }
            }
        }
    }

    fun showMessage(msg: String) {
        viewModelScope.launch {
            _snackBarMessage.emit(msg)
        }
    }

    fun startScan() {
        bleManager.startScan()
    }

    fun stopScan() {
        bleManager.stopScan()
    }

    fun connectDevice(device: BipDevice) {
        bleManager.connectToDevice(device) { connectedDev ->
            viewModelScope.launch {
                dao.insertOrUpdateDevice(connectedDev)
                showMessage("Connected to ${connectedDev.name} (${connectedDev.model})")
            }
        }
    }

    fun disconnectDevice() {
        bleManager.disconnect()
        viewModelScope.launch {
            activeDevice.value?.let { dev ->
                dao.updateConnectionStatus(dev.macAddress, false)
            }
            showMessage("Disconnected from watch")
        }
    }

    fun findWatch() {
        bleManager.findWatch {
            showMessage("Vibrating Bip Max watch!")
        }
    }

    fun syncDataNow() {
        bleManager.syncTime {
            viewModelScope.launch {
                val current = latestHealth.value
                val updatedSteps = liveSteps.value
                val updatedHr = liveHeartRate.value
                val newRecord = (current ?: HealthMetricRecord(
                    dateString = "Today",
                    steps = updatedSteps,
                    caloriesBurned = (updatedSteps * 0.045).toInt(),
                    distanceMeters = (updatedSteps * 0.75).toInt(),
                    currentHeartRateBpm = updatedHr,
                    restingHeartRateBpm = 62,
                    maxHeartRateBpm = 142,
                    minHeartRateBpm = 55,
                    spO2Percentage = 98,
                    stressLevel = 26,
                    paiScore = 85,
                    totalSleepMinutes = 460,
                    deepSleepMinutes = 110,
                    lightSleepMinutes = 240,
                    remSleepMinutes = 88,
                    awakeMinutes = 22
                )).copy(
                    timestamp = System.currentTimeMillis(),
                    steps = updatedSteps,
                    currentHeartRateBpm = updatedHr,
                    caloriesBurned = (updatedSteps * 0.045).toInt(),
                    distanceMeters = (updatedSteps * 0.75).toInt()
                )
                dao.insertHealthMetric(newRecord)
                showMessage("Bip Max telemetry & RTC clock successfully synced!")
            }
        }
    }

    fun installWatchFace(watchFace: WatchFace) {
        bleManager.installWatchFace(watchFace.id, watchFace.title, watchFace.fileSizeKb) {
            viewModelScope.launch {
                dao.setInstalledWatchFace(watchFace.id)
                showMessage("'${watchFace.title}' applied to Bip Max!")
            }
        }
    }

    fun saveAndFlashCustomWatchFace(config: CustomWatchFaceConfig) {
        val customId = "wf_custom_" + System.currentTimeMillis()
        val customFace = WatchFace(
            id = customId,
            title = "Custom ${config.backgroundStyle}",
            style = "Personalized",
            author = "Me",
            fileSizeKb = 256,
            isInstalled = true,
            isDefault = false,
            primaryColorHex = config.themeColorHex,
            accentColorHex = "#FFFFFF",
            description = "Custom configured Bip Max watchface with ${config.backgroundStyle} theme."
        )
        viewModelScope.launch {
            dao.insertWatchFaces(listOf(customFace))
            installWatchFace(customFace)
        }
    }

    fun toggleNotificationRule(rule: AppNotificationRule) {
        viewModelScope.launch {
            val updated = rule.copy(isEnabled = !rule.isEnabled)
            dao.updateNotificationRule(updated)
            showMessage("${rule.appName} notifications ${if (updated.isEnabled) "enabled" else "disabled"}")
        }
    }

    fun updateVibrationPattern(rule: AppNotificationRule, pattern: VibrationPattern) {
        viewModelScope.launch {
            val updated = rule.copy(vibrationPattern = pattern)
            dao.updateNotificationRule(updated)
            showMessage("Updated vibration pattern for ${rule.appName}")
        }
    }

    fun updateWatchSettings(newSettings: WatchSettings) {
        _watchSettings.value = newSettings
        showMessage("Watch preferences synced to Bip Max")
    }

    fun sendTestNotification(title: String, body: String, appName: String) {
        bleManager.sendNotificationAlert(title, body, appName)
        showMessage("Sent test alert: '$title' to Bip Max")
    }

    // Workout logic
    fun startWorkout(type: WorkoutType) {
        _activeWorkoutType.value = type
        _activeWorkoutSeconds.value = 0L
        _activeWorkoutDistanceKm.value = 0.0f
        _activeWorkoutCalories.value = 0
        _isWorkoutActive.value = true
        _isWorkoutPaused.value = false

        workoutJob?.cancel()
        workoutJob = viewModelScope.launch {
            while (_isWorkoutActive.value) {
                delay(1000)
                if (!_isWorkoutPaused.value) {
                    _activeWorkoutSeconds.value += 1
                    // increment distance and calories gradually
                    _activeWorkoutDistanceKm.value += when (type) {
                        WorkoutType.OUTDOOR_CYCLING -> 0.006f // ~21 km/h
                        WorkoutType.OUTDOOR_RUN -> 0.003f // ~10.8 km/h
                        WorkoutType.OUTDOOR_WALK -> 0.0014f // ~5 km/h
                        WorkoutType.POOL_SWIMMING -> 0.0008f
                        else -> 0.001f
                    }
                    if (_activeWorkoutSeconds.value % 6 == 0L) {
                        _activeWorkoutCalories.value += 1
                    }
                }
            }
        }
    }

    fun pauseWorkout() {
        _isWorkoutPaused.value = true
    }

    fun resumeWorkout() {
        _isWorkoutPaused.value = false
    }

    fun stopAndSaveWorkout() {
        val duration = _activeWorkoutSeconds.value
        val distance = _activeWorkoutDistanceKm.value
        val calories = _activeWorkoutCalories.value
        val type = _activeWorkoutType.value
        val avgHr = liveHeartRate.value.coerceAtLeast(110)

        val paceMinutes = if (distance > 0.05f) {
            val totalMin = duration / 60.0f
            val pacePerKm = totalMin / distance
            val min = pacePerKm.toInt()
            val sec = ((pacePerKm - min) * 60).toInt()
            String.format(Locale.US, "%d'%02d\"", min, sec)
        } else {
            "0'00\""
        }

        val record = WorkoutRecord(
            type = type,
            durationSeconds = duration,
            distanceKm = (distance * 100).toInt() / 100.0f,
            caloriesBurned = calories.coerceAtLeast(12),
            avgHeartRateBpm = avgHr,
            maxHeartRateBpm = avgHr + 18,
            avgPacePerKm = paceMinutes,
            timestamp = System.currentTimeMillis()
        )

        _isWorkoutActive.value = false
        _isWorkoutPaused.value = false
        workoutJob?.cancel()

        viewModelScope.launch {
            dao.insertWorkout(record)
            showMessage("Workout saved: ${type.title} (${record.distanceKm} km, ${record.caloriesBurned} kcal)")
        }
    }
}

package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SettingsRepository
import com.example.model.CameraZoomMode
import com.example.model.EngineStatus
import com.example.model.GameUiState
import com.example.model.TimeOfDayPreset
import com.example.model.UserSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application.applicationContext)

    val settings: StateFlow<UserSettings> = settingsRepository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserSettings()
    )

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var toastDismissJob: Job? = null

    fun onEngineReady() {
        _uiState.update {
            it.copy(
                engineStatus = EngineStatus.READY,
                engineErrorMessage = null
            )
        }
    }

    fun onEngineError(message: String) {
        _uiState.update {
            it.copy(
                engineStatus = EngineStatus.ERROR,
                engineErrorMessage = message
            )
        }
    }

    fun onRetryEngine() {
        _uiState.update {
            it.copy(
                engineStatus = EngineStatus.LOADING,
                engineErrorMessage = null
            )
        }
    }

    fun launchCity() {
        _uiState.update { it.copy(showSplash = false) }
    }

    fun returnToMenu() {
        _uiState.update { it.copy(showSplash = true, showDirectorySheet = false) }
    }

    fun setDirectorySheetVisible(visible: Boolean) {
        _uiState.update { it.copy(showDirectorySheet = visible) }
    }

    fun onTelemetryJson(jsonString: String) {
        runCatching {
            val json = JSONObject(jsonString)
            val promptRaw = json.optString("prompt", "")
            _uiState.update { current ->
                current.copy(
                    playerX = json.optDouble("x", current.playerX.toDouble()).toFloat(),
                    playerY = json.optDouble("y", current.playerY.toDouble()).toFloat(),
                    playerZ = json.optDouble("z", current.playerZ.toDouble()).toFloat(),
                    cameraYaw = json.optDouble("yaw", current.cameraYaw.toDouble()).toFloat(),
                    credits = json.optInt("credits", current.credits),
                    stamina = json.optInt("stamina", current.stamina).coerceIn(0, 100),
                    zoneLabel = json.optString("zone", current.zoneLabel),
                    interactPrompt = promptRaw.ifBlank { null },
                    isSeated = json.optBoolean("isSeated", current.isSeated),
                    isSprinting = json.optBoolean("isSprinting", current.isSprinting),
                    timePreset = TimeOfDayPreset.fromIndex(json.optInt("timePreset", current.timePreset.index)),
                    isRaining = json.optBoolean("isRaining", current.isRaining),
                    camMode = CameraZoomMode.fromIndex(json.optInt("camMode", current.camMode.index))
                )
            }
        }
    }

    fun showToast(message: String) {
        _uiState.update { it.copy(toastMessage = message) }
        toastDismissJob?.cancel()
        toastDismissJob = viewModelScope.launch {
            delay(2800L)
            _uiState.update { it.copy(toastMessage = null) }
        }
    }

    fun updateTimePreset(preset: TimeOfDayPreset) {
        _uiState.update { it.copy(timePreset = preset) }
    }

    fun updateRain(enabled: Boolean) {
        _uiState.update { it.copy(isRaining = enabled) }
    }

    fun updateCameraMode(mode: CameraZoomMode) {
        _uiState.update { it.copy(camMode = mode) }
    }

    fun setCameraSensitivity(value: Float) {
        viewModelScope.launch {
            settingsRepository.setCameraSensitivity(value)
        }
    }

    fun setAudioEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAudioEnabled(enabled)
        }
    }

    fun setHighResRender(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHighResRender(enabled)
        }
    }
}

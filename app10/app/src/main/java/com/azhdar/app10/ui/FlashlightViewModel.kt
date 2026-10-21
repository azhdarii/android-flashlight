package com.azhdar.app10.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.azhdar.app10.AppContainer
import com.azhdar.app10.core.flashlight.StrobeState
import com.azhdar.app10.core.flashlight.TorchState
import com.azhdar.app10.data.SettingsRepository
import com.azhdar.app10.data.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Exposes the flashlight state to the UI and forwards user intents to the
 * [com.azhdar.app10.core.flashlight.TorchController].
 *
 * The controller lives in the application scope and owns the actual torch, so the state
 * survives configuration changes and the app being backgrounded. The ViewModel only translates
 * intents and publishes the resulting state; it never touches the camera itself.
 */
class FlashlightViewModel(
    application: Application,
    container: AppContainer,
) : AndroidViewModel(application) {

    private val torchController = container.torchController
    private val settingsRepository = container.settingsRepository

    val torchState: StateFlow<TorchState> = torchController.torchState

    val strobeState: StateFlow<StrobeState> = torchController.strobeState

    val hasFlash: Boolean = torchController.hasFlash

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    val keepScreenOn: StateFlow<Boolean> = settingsRepository.keepScreenOn
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    init {
        viewModelScope.launch {
            settingsRepository.strobeIntervalMs.collect { interval ->
                if (!strobeState.value.isActive) {
                    torchController.updateStrobeInterval(interval)
                }
            }
        }
    }

    /** Turns the torch on when it is off and off when it is on. */
    fun toggleTorch() {
        if (torchState.value.isOn) {
            torchController.stopStrobe()
            torchController.setTorch(false)
        } else {
            torchController.setTorch(true).onFailure { error ->
                _errorMessage.value = error.message
            }
        }
    }

    /** Starts or stops the strobe (flasher) effect. */
    fun toggleStrobe() {
        if (strobeState.value.isActive) {
            torchController.stopStrobe()
        } else {
            torchController.startStrobe(strobeState.value.intervalMs)
        }
    }

    /** Updates the strobe period and persists it. */
    fun setStrobeInterval(millis: Int) {
        torchController.updateStrobeInterval(millis)
        viewModelScope.launch { settingsRepository.setStrobeInterval(millis) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setKeepScreenOn(enabled) }
    }

    fun consumeError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        // The torch and the strobe are owned by the application scoped controller, so they
        // intentionally keep running while the app is in the background. The torch callback
        // is registered by the application, not by this ViewModel, so it stays active for
        // as long as the light is on.
    }
}

package com.azhdar.app10.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.azhdar.app10.AppContainer
import com.azhdar.app10.core.flashlight.TorchState
import com.azhdar.app10.data.SettingsRepository
import com.azhdar.app10.data.ThemeMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Owns the flashlight state machine.
 *
 * Fixes compared to the original implementation:
 *  - The strobe loop used to be launched in a detached `CoroutineScope(Dispatchers.IO)` on every
 *    tap, so toggling it twice left an orphaned loop running and the torch stuck on. The loop
 *    now lives in [viewModelScope] and is tracked by a [Job] that is cancelled on stop.
 *  - The torch is switched off when the ViewModel is cleared, so leaving the app never leaves
 *    the light burning.
 */
class FlashlightViewModel(
    application: Application,
    container: AppContainer,
) : AndroidViewModel(application) {

    private val torchController = container.torchController
    private val settingsRepository = container.settingsRepository

    val torchState: StateFlow<TorchState> = torchController.torchState

    val hasFlash: Boolean = torchController.hasFlash

    private val _strobeActive = MutableStateFlow(false)
    val strobeActive: StateFlow<Boolean> = _strobeActive.asStateFlow()

    private val _strobeIntervalMs = MutableStateFlow(SettingsRepository.DEFAULT_STROBE_INTERVAL_MS)
    val strobeIntervalMs: StateFlow<Int> = _strobeIntervalMs.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    val keepScreenOn: StateFlow<Boolean> = settingsRepository.keepScreenOn
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private var strobeJob: Job? = null

    init {
        torchController.attach()
        viewModelScope.launch {
            settingsRepository.strobeIntervalMs.collect { interval ->
                _strobeIntervalMs.value = interval
            }
        }
    }

    /** Turns the torch on when it is off and off when it is on. */
    fun toggleTorch() {
        if (torchState.value.isOn) {
            stopStrobe()
            torchController.setTorch(false)
        } else {
            torchController.setTorch(true).onFailure { error ->
                _errorMessage.value = error.message
            }
        }
    }

    /** Starts or stops the strobe (flasher) effect. */
    fun toggleStrobe() {
        if (strobeJob?.isActive == true) {
            stopStrobe()
        } else {
            startStrobe()
        }
    }

    private fun startStrobe() {
        if (!hasFlash) return
        strobeJob?.cancel()
        strobeJob = viewModelScope.launch {
            _strobeActive.value = true
            try {
                while (isActive) {
                    val half = (_strobeIntervalMs.value / 2).coerceAtLeast(MIN_HALF_INTERVAL_MS).toLong()
                    torchController.setTorch(true)
                    delay(half)
                    torchController.setTorch(false)
                    delay(half)
                }
            } finally {
                _strobeActive.value = false
                torchController.safeSetTorchOff()
            }
        }
    }

    private fun stopStrobe() {
        strobeJob?.cancel()
        strobeJob = null
        _strobeActive.value = false
        torchController.safeSetTorchOff()
    }

    /** Updates the strobe period and persists it. */
    fun setStrobeInterval(millis: Int) {
        _strobeIntervalMs.value = millis
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
        stopStrobe()
        torchController.safeSetTorchOff()
        torchController.detach()
    }

    companion object {
        private const val MIN_HALF_INTERVAL_MS = 80
    }
}

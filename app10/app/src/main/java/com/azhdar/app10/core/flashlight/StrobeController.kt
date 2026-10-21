package com.azhdar.app10.core.flashlight

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Drives the strobe (flasher) effect.
 *
 * The controller is deliberately independent of the camera: it only needs to know how to
 * switch the torch on and off, which makes the timing and the state handling testable.
 *
 * Reliability notes:
 *  - Every run owns a generation number. A run may only reset the published state while its
 *    generation is still the current one. Without that guard the cleanup block of a
 *    cancelled run would mark a freshly started strobe as inactive, leaving the UI out of
 *    sync with the actual light.
 *  - The loop is bound to the scope it is given, so it is cancelled deterministically and
 *    can never outlive the process.
 */
class StrobeController(
    private val driver: TorchDriver,
    private val scope: CoroutineScope,
) {

    private val _state = MutableStateFlow(StrobeState())
    val state: StateFlow<StrobeState> = _state.asStateFlow()

    private var job: Job? = null

    @Volatile
    private var generation = 0

    /** True while the strobe is running. */
    val isActive: Boolean get() = _state.value.isActive

    /** The period of one flash, in milliseconds. */
    val intervalMs: Int get() = _state.value.intervalMs

    /** Starts the strobe with the given period. */
    fun start(intervalMs: Int) {
        job?.cancel()
        job = null
        val currentGeneration = ++generation

        // The state is published synchronously, so the UI reflects the new interval
        // immediately instead of one frame later.
        _state.value = _state.value.copy(isActive = true, intervalMs = intervalMs)

        job = scope.launch {
            try {
                val half = (intervalMs / 2).coerceAtLeast(MIN_HALF_INTERVAL_MS).toLong()
                while (isActive) {
                    driver.setTorch(true)
                    delay(half)
                    driver.setTorch(false)
                    delay(half)
                }
            } finally {
                if (currentGeneration == generation) {
                    job = null
                    _state.value = _state.value.copy(isActive = false)
                    driver.setTorch(false)
                }
            }
        }
    }

    /** Stops the strobe and switches the torch off. */
    fun stop() {
        job?.cancel()
        job = null
        generation++
        _state.value = _state.value.copy(isActive = false)
        driver.setTorch(false)
    }

    /** Updates the period used by the next activation. */
    fun updateInterval(intervalMs: Int) {
        _state.value = _state.value.copy(intervalMs = intervalMs)
    }

    companion object {
        const val MIN_HALF_INTERVAL_MS = 80
    }
}

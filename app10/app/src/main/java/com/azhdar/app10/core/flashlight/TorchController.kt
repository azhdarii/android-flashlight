package com.azhdar.app10.core.flashlight

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import com.azhdar.app10.core.service.TorchService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Owns the device torch.
 *
 * The instance lives in the application scoped [com.azhdar.app10.AppContainer], so the torch
 * survives configuration changes and the app being backgrounded. It is the single source of
 * truth for the torch state; the UI only observes it.
 *
 * Implementation notes:
 *  - the camera that actually owns a flash unit is located through [CameraCharacteristics]
 *    instead of assuming `cameraIdList[0]`;
 *  - the platform is allowed to change the torch behind our back (another app turning it off,
 *    the camera being taken over by a video call), so a [CameraManager.TorchCallback] keeps the
 *    published state in sync;
 *  - the strobe loop is bound to an application scope, so it keeps running while the app is in
 *    the background, and it is cancelled deterministically instead of leaking a coroutine.
 */
class TorchController(context: Context) {

    private val appContext: Context = context.applicationContext

    private val cameraManager: CameraManager =
        appContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    /**
     * Application wide scope. The strobe must survive the activity being destroyed, and it
     * must never outlive the process, which this scope guarantees.
     */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _torchState = MutableStateFlow(TorchState())
    val torchState: StateFlow<TorchState> = _torchState.asStateFlow()

    private val _strobeState = MutableStateFlow(StrobeState())
    val strobeState: StateFlow<StrobeState> = _strobeState.asStateFlow()

    /** True when the device exposes at least one camera with a flash unit. */
    val hasFlash: Boolean by lazy { findFlashCameraId() != null }

    private var flashCameraId: String? = null
    private var torchCallback: CameraManager.TorchCallback? = null
    private var strobeJob: Job? = null

    /** Tracks whether the foreground service is currently running. */
    private val serviceRunning = AtomicBoolean(false)

    private val callback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            if (cameraId != flashCameraId) return
            _torchState.value = _torchState.value.copy(isOn = enabled, available = true)
        }

        override fun onTorchModeUnavailable(cameraId: String) {
            if (cameraId != flashCameraId) return
            // Another app grabbed the camera; the torch is gone and the strobe has to stop.
            _torchState.value = _torchState.value.copy(isOn = false, available = false)
            stopStrobe()
        }
    }

    /**
     * Scans the camera list and returns the id of the first camera reporting a flash unit.
     * Returns `null` when the device has no flash at all.
     */
    private fun findFlashCameraId(): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null
        return try {
            cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Unable to enumerate cameras", e)
            null
        }
    }

    /** Registers the torch callback so external state changes are observed. */
    fun attach() {
        if (torchCallback != null) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        flashCameraId = flashCameraId ?: findFlashCameraId() ?: return
        try {
            cameraManager.registerTorchCallback(callback, null)
            torchCallback = callback
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Unable to register torch callback", e)
        }
    }

    /** Unregisters the torch callback. Safe to call multiple times. */
    fun detach() {
        val registered = torchCallback ?: return
        torchCallback = null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        try {
            cameraManager.unregisterTorchCallback(registered)
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Unable to unregister torch callback", e)
        }
    }

    /**
     * Turns the torch on or off.
     *
     * @return [Result.success] when the platform accepted the request, [Result.failure]
     *   otherwise. The published state is only updated on success.
     */
    fun setTorch(enabled: Boolean): Result<Unit> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return Result.failure(UnsupportedOperationException("Torch requires API 23+"))
        }
        val id = flashCameraId ?: findFlashCameraId()?.also { flashCameraId = it }
        if (id == null) {
            return Result.failure(IllegalStateException("No flash unit available on this device"))
        }
        return try {
            cameraManager.setTorchMode(id, enabled)
            _torchState.value = _torchState.value.copy(isOn = enabled)
            syncService(enabled)
            Result.success(Unit)
        } catch (e: CameraAccessException) {
            Log.e(TAG, "setTorchMode failed", e)
            Result.failure(e)
        } catch (e: IllegalArgumentException) {
            // Thrown when the camera does not support the torch at all.
            Log.e(TAG, "Camera does not support torch", e)
            Result.failure(e)
        }
    }

    /**
     * Keeps the foreground service in sync with the torch.
     *
     * The service is only alive while the light is on, so the notification is not shown
     * pointlessly and the process is released as soon as the light is off.
     *
     * The transition is guarded, because [TorchService.start] is a foreground service start
     * and Android 12+ rejects those when the app is in the background. The strobe toggles
     * the torch several times per second, so the service must only be started on the
     * actual off to on transition.
     */
    private fun syncService(enabled: Boolean) {
        if (enabled) {
            if (serviceRunning.compareAndSet(false, true)) {
                TorchService.start(appContext)
            }
        } else if (!_strobeState.value.isActive && serviceRunning.compareAndSet(true, false)) {
            TorchService.stop(appContext)
        }
    }

    /**
     * Called by [TorchService] when it is destroyed, so the next activation can start it
     * again.
     */
    fun onServiceStopped() {
        serviceRunning.set(false)
    }

    /** Turns the torch off, ignoring failures. Used for cleanup paths. */
    fun safeSetTorchOff() {
        runCatching { setTorch(false) }
    }

    /** Starts the strobe (flasher) effect with the given period. */
    fun startStrobe(intervalMs: Int) {
        if (!hasFlash) return
        stopStrobe()
        strobeJob = scope.launch {
            _strobeState.value = _strobeState.value.copy(isActive = true, intervalMs = intervalMs)
            try {
                val half = (intervalMs / 2).coerceAtLeast(MIN_HALF_INTERVAL_MS).toLong()
                while (isActive) {
                    setTorch(true)
                    delay(half)
                    setTorch(false)
                    delay(half)
                }
            } finally {
                _strobeState.value = _strobeState.value.copy(isActive = false)
                safeSetTorchOff()
            }
        }
    }

    /** Stops the strobe effect and switches the torch off. */
    fun stopStrobe() {
        strobeJob?.cancel()
        strobeJob = null
        _strobeState.value = _strobeState.value.copy(isActive = false)
        safeSetTorchOff()
    }

    /** Updates the period used by the strobe. Takes effect on the next activation. */
    fun updateStrobeInterval(intervalMs: Int) {
        _strobeState.value = _strobeState.value.copy(intervalMs = intervalMs)
    }

    /** Releases every resource held by the controller. */
    fun release() {
        stopStrobe()
        safeSetTorchOff()
        detach()
    }

    companion object {
        private const val TAG = "TorchController"
        private const val MIN_HALF_INTERVAL_MS = 80
    }
}

/** Snapshot of the torch state exposed to the UI layer. */
data class TorchState(
    val isOn: Boolean = false,
    val available: Boolean = true,
)

/** Snapshot of the strobe state exposed to the UI layer. */
data class StrobeState(
    val isActive: Boolean = false,
    val intervalMs: Int = 800,
)

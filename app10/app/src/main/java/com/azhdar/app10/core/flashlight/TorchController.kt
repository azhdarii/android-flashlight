package com.azhdar.app10.core.flashlight

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Thin, testable wrapper around the camera2 torch API.
 *
 * Responsibilities:
 *  - locating the camera that actually owns a flash unit (the old code blindly used
 *    `cameraIdList[0]`, which is the back camera on most devices but is not guaranteed);
 *  - reporting torch state changes coming from the platform (e.g. another app turning the
 *    torch off) so the UI never shows a stale "on" state;
 *  - translating the checked [CameraAccessException] into a result the UI can react to.
 */
class TorchController(context: Context) {

    private val cameraManager: CameraManager =
        context.applicationContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private val _torchState = MutableStateFlow(TorchState())
    val torchState: StateFlow<TorchState> = _torchState.asStateFlow()

    /** True when the device exposes at least one camera with a flash unit. */
    val hasFlash: Boolean by lazy { findFlashCameraId() != null }

    private var flashCameraId: String? = null
    private var torchCallback: CameraManager.TorchCallback? = null

    private val callback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            if (cameraId != flashCameraId) return
            _torchState.value = _torchState.value.copy(isOn = enabled, available = true)
        }

        override fun onTorchModeUnavailable(cameraId: String) {
            if (cameraId != flashCameraId) return
            _torchState.value = _torchState.value.copy(isOn = false, available = false)
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
     *   otherwise. The torch state is only updated on success.
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

    /** Turns the torch off, ignoring failures. Used for cleanup paths. */
    fun safeSetTorchOff() {
        runCatching { setTorch(false) }
    }

    companion object {
        private const val TAG = "TorchController"
    }
}

/** Snapshot of the torch state exposed to the UI layer. */
data class TorchState(
    val isOn: Boolean = false,
    val available: Boolean = true,
)

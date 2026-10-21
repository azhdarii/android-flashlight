package com.azhdar.app10.core.flashlight

/**
 * Abstraction over the physical torch.
 *
 * It exists so that the strobe logic can be exercised without a camera, and so that the
 * [TorchController] stays focused on the platform specifics.
 */
interface TorchDriver {
    /**
     * Switches the torch on or off.
     *
     * @return [Result.success] when the platform accepted the request, [Result.failure]
     *   otherwise.
     */
    fun setTorch(enabled: Boolean): Result<Unit>

    /** The current state of the torch. */
    val isTorchOn: Boolean
}

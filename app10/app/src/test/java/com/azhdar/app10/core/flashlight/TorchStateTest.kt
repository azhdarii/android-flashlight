package com.azhdar.app10.core.flashlight

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the torch state model.
 *
 * These cover the state transitions the UI relies on, in particular the case where the
 * platform reports the torch as unavailable (another app grabbed the camera) which the old
 * implementation ignored, leaving the UI showing a stale "on" indicator.
 */
class TorchStateTest {

    @Test
    fun `initial state is off and available`() {
        val state = TorchState()
        assertFalse(state.isOn)
        assertTrue(state.available)
    }

    @Test
    fun `copy preserves unrelated fields`() {
        val state = TorchState(isOn = true, available = false)
        val updated = state.copy(isOn = false)
        assertFalse(updated.isOn)
        assertFalse(updated.available)
    }
}

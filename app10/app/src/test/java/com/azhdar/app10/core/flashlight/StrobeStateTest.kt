package com.azhdar.app10.core.flashlight

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests the state models that the UI renders.
 */
class StrobeStateTest {

    @Test
    fun `strobe is inactive by default`() {
        val state = StrobeState()
        assertFalse(state.isActive)
        assertEquals(800, state.intervalMs)
    }

    @Test
    fun `strobe state keeps the interval when the activity changes`() {
        val state = StrobeState(isActive = true, intervalMs = 250)
        val updated = state.copy(isActive = false)
        assertFalse(updated.isActive)
        assertEquals(250, updated.intervalMs)
    }

    @Test
    fun `torch state defaults to off and available`() {
        val state = TorchState()
        assertFalse(state.isOn)
        assertTrue(state.available)
    }
}

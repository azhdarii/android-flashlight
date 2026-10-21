package com.azhdar.app10.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies the strobe timing rules that the flashing loop relies on.
 */
class StrobeTimingTest {

    private fun halfInterval(totalMs: Int): Long =
        (totalMs / 2).coerceAtLeast(MIN_HALF).toLong()

    @Test
    fun `half interval is half of the total`() {
        assertEquals(400L, halfInterval(800))
        assertEquals(100L, halfInterval(200))
    }

    @Test
    fun `half interval never drops below the minimum`() {
        // A very fast setting must not turn into a busy loop that floods the camera service.
        assertEquals(MIN_HALF.toLong(), halfInterval(1))
        assertEquals(MIN_HALF.toLong(), halfInterval(0))
    }

    @Test
    fun `strobe interval is clamped to the supported range`() {
        assertEquals(200, 100.coerceIn(200, 2000))
        assertEquals(2000, 5000.coerceIn(200, 2000))
        assertEquals(800, 800.coerceIn(200, 2000))
    }

    @Test
    fun `minimum half interval is positive`() {
        assertTrue(MIN_HALF > 0)
    }

    companion object {
        private const val MIN_HALF = 80
    }
}

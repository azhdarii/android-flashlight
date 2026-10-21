package com.azhdar.app10.core.flashlight

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Records the calls a [TorchDriver] receives. */
private class FakeTorchDriver : TorchDriver {
    val calls = mutableListOf<Boolean>()

    override var isTorchOn: Boolean = false
        private set

    override fun setTorch(enabled: Boolean): Result<Unit> {
        calls += enabled
        isTorchOn = enabled
        return Result.success(Unit)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class StrobeControllerTest {

    @Test
    fun `strobe is inactive before it is started`() = runTest {
        val driver = FakeTorchDriver()
        val strobe = StrobeController(driver, TestScope(testScheduler))

        assertFalse(strobe.isActive)
        assertTrue(driver.calls.isEmpty())
    }

    @Test
    fun `strobe alternates the torch and stops when cancelled`() = runTest {
        val driver = FakeTorchDriver()
        val strobe = StrobeController(driver, this)

        strobe.start(400)
        advanceTimeBy(1000)

        assertTrue(strobe.isActive)
        // The torch is toggled several times, alternating on and off.
        assertTrue(driver.calls.size >= 4)
        assertTrue(driver.calls.first())
        // Every call alternates, so the torch is never left in an undefined state.
        driver.calls.zipWithNext().forEach { (first, second) ->
            assertTrue(first != second)
        }

        strobe.stop()
        assertFalse(strobe.isActive)
        // Stopping always leaves the torch off.
        assertFalse(driver.isTorchOn)
    }

    @Test
    fun `starting the strobe twice does not leave an orphaned loop`() = runTest {
        val driver = FakeTorchDriver()
        val strobe = StrobeController(driver, this)

        strobe.start(400)
        advanceTimeBy(200)
        strobe.start(400)
        advanceTimeBy(200)

        // The cancelled run must not reset the state of the run that replaced it. This was
        // the bug that left the UI showing "strobe off" while the light kept flashing.
        assertTrue(strobe.isActive)

        strobe.stop()
        assertFalse(strobe.isActive)
        assertFalse(driver.isTorchOn)
    }

    @Test
    fun `stopping the strobe switches the torch off`() = runTest {
        val driver = FakeTorchDriver()
        val strobe = StrobeController(driver, this)

        strobe.start(200)
        advanceTimeBy(250)
        strobe.stop()

        assertFalse(driver.isTorchOn)
    }

    @Test
    fun `the interval is published and can be updated`() = runTest {
        val driver = FakeTorchDriver()
        val strobe = StrobeController(driver, this)

        strobe.start(600)
        assertEquals(600, strobe.intervalMs)

        strobe.updateInterval(1200)
        assertEquals(1200, strobe.intervalMs)

        // The strobe must be stopped, otherwise the test coroutine never completes.
        strobe.stop()
    }
}

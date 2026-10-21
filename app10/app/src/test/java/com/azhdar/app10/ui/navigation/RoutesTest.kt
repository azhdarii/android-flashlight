package com.azhdar.app10.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies the navigation routes.
 *
 * The selected colour is now carried through a navigation argument rather than a mutable
 * field on the Activity, so it survives process death and cannot leak the Activity.
 */
class RoutesTest {

    @Test
    fun `solid color route embeds the color value`() {
        val color = 0xFF00FF00.toInt()
        val route = Routes.SolidColor.createRoute(color)
        assertTrue(route.startsWith("solid_color/"))
        assertEquals(color, route.substringAfterLast('/').toInt())
    }

    @Test
    fun `solid color route keeps negative argb values intact`() {
        // White is 0xFFFFFFFF which overflows into a negative Int.
        val color = 0xFFFFFFFF.toInt()
        val route = Routes.SolidColor.createRoute(color)
        assertEquals(color, route.substringAfterLast('/').toInt())
    }

    @Test
    fun `routes are unique`() {
        val routes = listOf(
            Routes.Home.route,
            Routes.ColorChooser.route,
            Routes.ColorPicker.route,
            Routes.SolidColor.route,
        )
        assertEquals(routes.size, routes.toSet().size)
    }
}

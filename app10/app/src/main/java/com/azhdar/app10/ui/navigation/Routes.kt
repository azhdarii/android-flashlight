package com.azhdar.app10.ui.navigation

/**
 * Type-safe navigation destinations.
 *
 * The original app used raw strings ("ui", "fragment", "alireza"...) and stored the selected
 * color in a mutable field on the Activity, which was lost on process death and leaked the
 * Activity into unrelated classes. Destinations are now declared once, and the color is
 * passed as a navigation argument.
 */
sealed class Routes(val route: String) {

    /** Main flashlight screen. */
    data object Home : Routes("home")

    /** Preset colour chooser. */
    data object ColorChooser : Routes("color_chooser")

    /** Full HSV colour picker. */
    data object ColorPicker : Routes("color_picker")

    /** Fullscreen solid colour. [COLOR_ARG] carries the ARGB value. */
    data object SolidColor : Routes("solid_color/{$COLOR_ARG}") {
        const val COLOR_ARG = "color"
        fun createRoute(color: Int): String = "solid_color/$color"
    }

    companion object {
        const val COLOR_ARG = "color"
    }
}

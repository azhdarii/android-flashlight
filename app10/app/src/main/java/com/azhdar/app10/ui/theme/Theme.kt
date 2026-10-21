package com.azhdar.app10.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.azhdar.app10.data.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = TorchAmber,
    onPrimary = OnAmberDark,
    primaryContainer = TorchAmberDark,
    onPrimaryContainer = TorchOnAmberContainer,
    secondary = SlateBlueDark,
    onSecondary = OnSlateDark,
    secondaryContainer = SlateContainerDark,
    onSecondaryContainer = SlateContainer,
    tertiary = SoftRedDark,
    onTertiary = OnSoftRedDark,
    tertiaryContainer = SoftRedContainer,
    onTertiaryContainer = OnSoftRedContainer,
    background = DarkBackground,
    onBackground = InkDark,
    surface = DarkBackground,
    onSurface = InkDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFC9C5D0),
)

private val LightColorScheme = lightColorScheme(
    primary = TorchAmberDark,
    onPrimary = Color.White,
    primaryContainer = TorchAmberContainer,
    onPrimaryContainer = TorchOnAmberContainer,
    secondary = SlateBlue,
    onSecondary = Color.White,
    secondaryContainer = SlateContainer,
    onSecondaryContainer = OnSlateContainer,
    tertiary = SoftRed,
    onTertiary = Color.White,
    tertiaryContainer = SoftRedContainer,
    onTertiaryContainer = OnSoftRedContainer,
    background = LightBackground,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
)

/**
 * Application theme.
 *
 * Improvements over the original:
 *  - the theme mode is now user selectable and persisted instead of always following the
 *    system setting;
 *  - dynamic (wallpaper) colour is opt-in, so the brand palette is not silently replaced on
 *    Android 12+;
 *  - the status bar appearance is derived from the resolved scheme in a single place, instead
 *    of being overwritten by every screen.
 */
@Composable
fun App10Theme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}

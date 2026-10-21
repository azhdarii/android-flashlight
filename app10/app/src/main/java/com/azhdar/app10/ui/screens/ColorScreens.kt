package com.azhdar.app10.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.azhdar.app10.R
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

/** The preset colours offered by the chooser screen. */
private val PresetColors = listOf(
    Color.White to R.string.preset_white,
    Color.Red to R.string.preset_red,
    Color.Green to R.string.preset_green,
    Color.Blue to R.string.preset_blue,
    Color.Yellow to R.string.preset_yellow,
    Color.Magenta to R.string.preset_magenta,
    Color.Cyan to R.string.preset_cyan,
    Color.Black to R.string.preset_black,
)

/**
 * Preset colour chooser.
 *
 * Replaces the old "fragment" screen which built an [IconButton] chain with hard coded
 * padding and stored the result in a mutable Activity field.
 */
@Composable
fun ColorChooserScreen(
    onColorSelected: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ScreenHeader(title = stringResource(R.string.color_chooser), onBack = onBack)

            Spacer(Modifier.height(16.dp))

            PresetColors.chunked(4).forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    row.forEach { (color, labelRes) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(color, CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                                        shape = CircleShape,
                                    )
                                    .clickable { onColorSelected(color.toArgbInt()) },
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = stringResource(labelRes),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full HSV colour picker.
 *
 * The original screen declared its picker inside a `ConstraintLayout` that was not
 * constrained to the parent, and the "select" button was a no-op placeholder.
 */
@Composable
fun ColorPickerScreen(
    onColorSelected: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val controller = rememberColorPickerController()
    var selectedColor by remember { mutableStateOf(Color.White) }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ScreenHeader(title = stringResource(R.string.color_picker), onBack = onBack)

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(selectedColor, CircleShape)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        shape = CircleShape,
                    ),
            )

            Spacer(Modifier.height(24.dp))

            HsvColorPicker(
                modifier = Modifier.size(280.dp),
                controller = controller,
                onColorChanged = { envelope: ColorEnvelope ->
                    selectedColor = envelope.color
                },
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { onColorSelected(selectedColor.toArgbInt()) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.select_color))
            }
        }
    }
}

/**
 * Fullscreen solid colour, used as a plain white/amber lamp.
 */
@Composable
fun SolidColorScreen(
    color: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(color))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = Color.Black.copy(alpha = 0.6f),
            )
        }
        Text(
            text = stringResource(R.string.tap_to_go_back),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black.copy(alpha = 0.45f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
        )
    }
}

@Composable
private fun ScreenHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
        Spacer(Modifier.size(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

/** Converts a Compose [Color] into a packed ARGB int. */
private fun Color.toArgbInt(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(),
    (red * 255).toInt(),
    (green * 255).toInt(),
    (blue * 255).toInt(),
)

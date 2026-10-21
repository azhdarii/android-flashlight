package com.azhdar.app10.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azhdar.app10.R
import com.azhdar.app10.core.ads.AdsManager
import com.azhdar.app10.data.SettingsRepository
import com.azhdar.app10.data.ThemeMode
import com.azhdar.app10.ui.FlashlightViewModel
import com.azhdar.app10.ui.components.AdBanner
import com.azhdar.app10.ui.components.TorchActionButton
import com.azhdar.app10.ui.components.TorchIcons
import com.azhdar.app10.ui.theme.App10Theme

/**
 * Main screen: torch, strobe and entry points to the colour screens and settings.
 */
@Composable
fun HomeScreen(
    viewModel: FlashlightViewModel,
    adsManager: AdsManager,
    onOpenColorChooser: () -> Unit,
    onOpenColorPicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val torchState by viewModel.torchState.collectAsStateWithLifecycle()
    val strobeState by viewModel.strobeState.collectAsStateWithLifecycle()
    val strobeActive = strobeState.isActive
    val hasFlash by remember { mutableStateOf(viewModel.hasFlash) }
    val strobeInterval = strobeState.intervalMs
    val keepScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Surface platform level failures (camera busy, no flash, ...) to the user instead of
    // silently swallowing them, which is what the original implementation did.
    LaunchedEffect(errorMessage) {
        val message = errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeError()
    }

    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            IconButton(onClick = { showSettings = true }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.open_settings),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        TorchActionButton(
            icon = if (torchState.isOn) TorchIcons.TorchOn else TorchIcons.TorchOff,
            contentDescription = stringResource(
                if (torchState.isOn) R.string.turn_torch_off else R.string.turn_torch_on,
            ),
            onClick = viewModel::toggleTorch,
            enabled = hasFlash,
            active = torchState.isOn,
            size = 160.dp,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(
                if (torchState.isOn) R.string.torch_is_on else R.string.torch_is_off,
            ),
            style = MaterialTheme.typography.titleMedium,
            color = if (torchState.isOn) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )

        if (!hasFlash) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.no_flash_available),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TorchActionButton(
                icon = TorchIcons.Strobe,
                contentDescription = stringResource(R.string.toggle_strobe),
                onClick = viewModel::toggleStrobe,
                enabled = hasFlash,
                active = strobeActive,
                size = 96.dp,
            )
            TorchActionButton(
                icon = Icons.Filled.ColorLens,
                contentDescription = stringResource(R.string.open_color_picker),
                onClick = onOpenColorPicker,
                size = 96.dp,
                activeColor = MaterialTheme.colorScheme.secondary,
            )
            TorchActionButton(
                icon = Icons.Filled.Palette,
                contentDescription = stringResource(R.string.open_color_chooser),
                onClick = onOpenColorChooser,
                size = 96.dp,
                activeColor = MaterialTheme.colorScheme.tertiary,
            )
        }

        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.strobe_speed),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.strobe_speed_value, strobeInterval),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Slider(
                    value = strobeInterval.toFloat(),
                    onValueChange = { viewModel.setStrobeInterval(it.toInt()) },
                    valueRange = SettingsRepository.MIN_STROBE_INTERVAL_MS.toFloat()..
                        SettingsRepository.MAX_STROBE_INTERVAL_MS.toFloat(),
                    steps = 8,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        AdBanner(adsManager = adsManager)

        Spacer(Modifier.height(16.dp))
    }
    }

    if (showSettings) {
        SettingsDialog(
            themeMode = themeMode,
            keepScreenOn = keepScreenOn,
            onThemeModeChange = viewModel::setThemeMode,
            onKeepScreenOnChange = viewModel::setKeepScreenOn,
            onDismiss = { showSettings = false },
        )
    }
}

@Composable
private fun SettingsDialog(
    themeMode: ThemeMode,
    keepScreenOn: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.keep_screen_on),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Switch(
                    checked = keepScreenOn,
                    onCheckedChange = onKeepScreenOnChange,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.theme),
                    style = MaterialTheme.typography.titleMedium,
                )
                ThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = { onThemeModeChange(mode) }) {
                            Text(
                                text = stringResource(mode.labelRes()),
                                color = if (mode == themeMode) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        },
    )
}

private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    App10Theme {
        Text("Home", modifier = Modifier.padding(16.dp))
    }
}

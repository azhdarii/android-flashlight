package com.azhdar.app10.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app10_settings")

/** User selectable theme mode. */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

/**
 * Persistent user preferences backed by Jetpack DataStore.
 *
 * The original app kept its state in a `ViewModel` with `mutableStateOf`, which meant every
 * restart lost the user's choice. Preferences now survive process death.
 */
class SettingsRepository(private val context: Context) {

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME_MODE]?.let { stored ->
            ThemeMode.entries.firstOrNull { it.name == stored }
        } ?: ThemeMode.SYSTEM
    }

    val keepScreenOn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_KEEP_SCREEN_ON] ?: true
    }

    val strobeIntervalMs: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_STROBE_INTERVAL] ?: DEFAULT_STROBE_INTERVAL_MS
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_KEEP_SCREEN_ON] = enabled
        }
    }

    suspend fun setStrobeInterval(millis: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_STROBE_INTERVAL] = millis.coerceIn(MIN_STROBE_INTERVAL_MS, MAX_STROBE_INTERVAL_MS)
        }
    }

    companion object {
        val DEFAULT_STROBE_INTERVAL_MS = 800
        val MIN_STROBE_INTERVAL_MS = 200
        val MAX_STROBE_INTERVAL_MS = 2000

        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        private val KEY_STROBE_INTERVAL = intPreferencesKey("strobe_interval_ms")
    }
}

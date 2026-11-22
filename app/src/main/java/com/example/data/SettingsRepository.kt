package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "wild_horizon_settings"
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val CAMERA_SENSITIVITY = floatPreferencesKey("camera_sensitivity")
        val AUDIO_ENABLED = booleanPreferencesKey("audio_enabled")
        val HIGH_RES_RENDER = booleanPreferencesKey("high_res_render")
    }

    val settingsFlow: Flow<UserSettings> = context.settingsDataStore.data.map { prefs ->
        UserSettings(
            cameraSensitivity = prefs[Keys.CAMERA_SENSITIVITY] ?: 1.0f,
            audioEnabled = prefs[Keys.AUDIO_ENABLED] ?: true,
            highResRender = prefs[Keys.HIGH_RES_RENDER] ?: true
        )
    }

    suspend fun setCameraSensitivity(value: Float) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.CAMERA_SENSITIVITY] = value.coerceIn(0.4f, 2.2f)
        }
    }

    suspend fun setAudioEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.AUDIO_ENABLED] = enabled
        }
    }

    suspend fun setHighResRender(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.HIGH_RES_RENDER] = enabled
        }
    }
}

package com.wod.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wod.app.domain.model.CueCategory
import com.wod.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wod_settings")

class SettingsDataStore(private val context: Context) {

    private val ds get() = context.dataStore

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val MASTER_ENABLED = booleanPreferencesKey("audio_master_enabled")
        val MASTER_VOLUME = floatPreferencesKey("audio_master_volume")
        val COUNTDOWN_ENABLED = booleanPreferencesKey("audio_countdown_enabled")
        val HALFWAY_ENABLED = booleanPreferencesKey("audio_halfway_enabled")
        val PHASE_TRANSITION_ENABLED = booleanPreferencesKey("audio_phase_transition_enabled")
        val COMPLETION_ENABLED = booleanPreferencesKey("audio_completion_enabled")
    }

    val themeModeFlow: Flow<ThemeMode> = ds.data
        .catchIo()
        .map { prefs ->
            runCatching { ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.DARK.name) }
                .getOrDefault(ThemeMode.DARK)
        }

    val masterEnabledFlow: Flow<Boolean> = ds.data.catchIo().map { it[Keys.MASTER_ENABLED] ?: true }

    val masterVolumeFlow: Flow<Float> = ds.data.catchIo().map { it[Keys.MASTER_VOLUME] ?: 1f }

    fun categoryEnabledFlow(category: CueCategory): Flow<Boolean> =
        ds.data.catchIo().map { it[categoryKey(category)] ?: true }

    suspend fun setThemeMode(mode: ThemeMode) { ds.edit { it[Keys.THEME_MODE] = mode.name } }

    suspend fun setMasterEnabled(enabled: Boolean) { ds.edit { it[Keys.MASTER_ENABLED] = enabled } }

    suspend fun setMasterVolume(volume: Float) {
        ds.edit { it[Keys.MASTER_VOLUME] = volume.coerceIn(0f, 1f) }
    }

    suspend fun setCategoryEnabled(category: CueCategory, enabled: Boolean) {
        ds.edit { it[categoryKey(category)] = enabled }
    }

    private fun categoryKey(category: CueCategory): Preferences.Key<Boolean> = when (category) {
        CueCategory.COUNTDOWN -> Keys.COUNTDOWN_ENABLED
        CueCategory.HALFWAY -> Keys.HALFWAY_ENABLED
        CueCategory.PHASE_TRANSITION -> Keys.PHASE_TRANSITION_ENABLED
        CueCategory.COMPLETION -> Keys.COMPLETION_ENABLED
    }

    private fun Flow<Preferences>.catchIo() = catch { e ->
        if (e is IOException) emit(emptyPreferences()) else throw e
    }
}

package com.wod.app.data.repository

import com.wod.app.data.datastore.SettingsDataStore
import com.wod.app.domain.model.CueCategory
import com.wod.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow

class PreferencesRepository(private val store: SettingsDataStore) {

    val themeModeFlow: Flow<ThemeMode> = store.themeModeFlow
    val masterEnabledFlow: Flow<Boolean> = store.masterEnabledFlow
    val masterVolumeFlow: Flow<Float> = store.masterVolumeFlow

    fun categoryEnabledFlow(category: CueCategory): Flow<Boolean> =
        store.categoryEnabledFlow(category)

    suspend fun setThemeMode(mode: ThemeMode) = store.setThemeMode(mode)
    suspend fun setMasterEnabled(enabled: Boolean) = store.setMasterEnabled(enabled)
    suspend fun setMasterVolume(volume: Float) = store.setMasterVolume(volume)
    suspend fun setCategoryEnabled(category: CueCategory, enabled: Boolean) =
        store.setCategoryEnabled(category, enabled)
}

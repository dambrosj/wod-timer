package com.wod.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wod.app.WodApp
import com.wod.app.domain.model.CueCategory
import com.wod.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AudioSettings(
    val masterEnabled: Boolean = true,
    val masterVolume: Float = 1f,
    val countdownEnabled: Boolean = true,
    val halfwayEnabled: Boolean = true,
    val phaseTransitionEnabled: Boolean = true,
    val completionEnabled: Boolean = true,
)

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val wodApp = app as WodApp
    private val prefs = wodApp.preferencesRepository

    val themeMode: StateFlow<ThemeMode> = prefs.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.DARK)

    val audioSettings: StateFlow<AudioSettings> = combine(
        combine(
            prefs.masterEnabledFlow,
            prefs.masterVolumeFlow,
            prefs.categoryEnabledFlow(CueCategory.COUNTDOWN),
        ) { masterEnabled, masterVolume, countdown ->
            Triple(masterEnabled, masterVolume, countdown)
        },
        combine(
            prefs.categoryEnabledFlow(CueCategory.HALFWAY),
            prefs.categoryEnabledFlow(CueCategory.PHASE_TRANSITION),
            prefs.categoryEnabledFlow(CueCategory.COMPLETION),
        ) { halfway, phaseTransition, completion ->
            Triple(halfway, phaseTransition, completion)
        },
    ) { (masterEnabled, masterVolume, countdown), (halfway, phaseTransition, completion) ->
        AudioSettings(
            masterEnabled          = masterEnabled,
            masterVolume           = masterVolume,
            countdownEnabled       = countdown,
            halfwayEnabled         = halfway,
            phaseTransitionEnabled = phaseTransition,
            completionEnabled      = completion,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AudioSettings())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { prefs.setThemeMode(mode) }
    }

    fun setMasterEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setMasterEnabled(enabled) }
    }

    fun setMasterVolume(volume: Float) {
        viewModelScope.launch { prefs.setMasterVolume(volume) }
    }

    fun setCategoryEnabled(category: CueCategory, enabled: Boolean) {
        viewModelScope.launch { prefs.setCategoryEnabled(category, enabled) }
    }

    /**
     * Applies [volume] immediately to the audio manager and plays a single
     * tick as auditory feedback for the volume slider.
     * Called from [SettingsScreen] on [Slider.onValueChangeFinished].
     */
    fun playVolumePreview(volume: Float) {
        // Apply directly — the DataStore update (setMasterVolume) is async and may
        // not have propagated to the manager yet via WodApp.collectLatest.
        wodApp.audioCueManager.setVolume(volume)
        wodApp.audioCueManager.playPreview()
    }
}

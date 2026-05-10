package com.wod.app

import android.app.Application
import com.wod.app.data.audio.AudioCueManager
import com.wod.app.data.audio.SoundPoolAudioCueManager
import com.wod.app.data.datastore.SettingsDataStore
import com.wod.app.data.db.WodDatabase
import com.wod.app.data.repository.PreferencesRepository
import com.wod.app.data.repository.SavedWodRepository
import com.wod.app.data.repository.WorkoutRepository
import com.wod.app.domain.model.CueCategory
import com.wod.app.domain.model.SavedWod
import com.wod.app.domain.model.TimerConfig
import com.wod.app.domain.model.TimerPhase
import com.wod.app.domain.model.WorkoutLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Service-locator style Application. Exposes singletons as lazy properties.
 * No DI framework — matches the tag-treaker pattern. See HANDOFF.md §5 rule 2.
 */
class WodApp : Application() {

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val audioCueManager: AudioCueManager by lazy {
        SoundPoolAudioCueManager(this).also { it.preload() }
    }

    val database: WodDatabase by lazy { WodDatabase.build(this) }

    val settingsDataStore: SettingsDataStore by lazy { SettingsDataStore(this) }

    val savedWodRepository: SavedWodRepository by lazy {
        SavedWodRepository(database.savedWodDao())
    }

    val workoutRepository: WorkoutRepository by lazy {
        WorkoutRepository(database.workoutLogDao())
    }

    val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepository(settingsDataStore)
    }

    /** Live phase pushed by TimerForegroundService; null when no timer is running. */
    val activePhase: MutableStateFlow<TimerPhase?> = MutableStateFlow(null)

    /** Emits one Unit when the active workout completes (engine finishes). */
    val timerCompleted: MutableSharedFlow<Unit> = MutableSharedFlow(replay = 0, extraBufferCapacity = 1)

    /** Config to start on the next timer screen entry. Set by WodNavGraph before navigating. */
    @Volatile var pendingConfig: TimerConfig? = null

    /** Completed workout waiting for user confirmation on CompletionScreen. Set by the service on completion. */
    @Volatile var pendingWorkoutLog: WorkoutLog? = null

    /** Log selected by the user for detail view. Set by DiaryScreen before navigating to detail. */
    @Volatile var selectedWorkoutLog: WorkoutLog? = null

    /** WOD selected by the user in WodsLibraryScreen; read by WodDetailScreen. */
    @Volatile var selectedWod: SavedWod? = null

    override fun onCreate() {
        super.onCreate()
        appScope.launch { savedWodRepository.seedBuiltIns() }
        // Apply persisted audio settings to the manager immediately and on every change.
        appScope.launch {
            preferencesRepository.masterEnabledFlow.collectLatest {
                audioCueManager.setMasterEnabled(it)
            }
        }
        appScope.launch {
            preferencesRepository.masterVolumeFlow.collectLatest {
                audioCueManager.setVolume(it)
            }
        }
        // Sync individual audio category toggles (countdown, halfway, phase, completion)
        CueCategory.entries.forEach { category ->
            appScope.launch {
                preferencesRepository.categoryEnabledFlow(category).collectLatest { enabled ->
                    audioCueManager.setCategoryEnabled(category, enabled)
                }
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        audioCueManager.release()
    }
}

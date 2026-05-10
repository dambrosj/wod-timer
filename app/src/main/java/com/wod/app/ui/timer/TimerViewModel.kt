package com.wod.app.ui.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.wod.app.WodApp
import com.wod.app.domain.model.TimerConfig
import com.wod.app.domain.model.TimerPhase
import com.wod.app.service.TimerForegroundService
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for TimerRunningScreen (T30).
 *
 * Scoped to the timer nav back-stack entry — cleared (and service stopped)
 * when the user navigates back or the workout completes.
 *
 * State is backed by [WodApp] singletons so the foreground service can update
 * them without a direct reference to this ViewModel.
 */
class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as WodApp

    /** Live phase — null until the first engine tick arrives. */
    val phase: StateFlow<TimerPhase?> = app.activePhase

    /** Fires once when the engine finishes all phases. */
    val isCompleted: SharedFlow<Unit> = app.timerCompleted

    /** Starts the ForegroundService with the config stored in [WodApp.pendingConfig]. */
    fun startTimer() {
        val config = app.pendingConfig ?: return
        when (config) {
            is TimerConfig.Tabata   -> TimerForegroundService.startTabata(app, config)
            is TimerConfig.Amrap    -> TimerForegroundService.startAmrap(app, config)
            is TimerConfig.ForTime  -> TimerForegroundService.startForTime(app, config)
            is TimerConfig.Emom     -> TimerForegroundService.startEmom(app, config)
            is TimerConfig.Mix      -> { /* MIX engine not yet implemented (T27+) */ }
        }
    }

    fun pause()  = TimerForegroundService.pause(getApplication())
    fun resume() = TimerForegroundService.resume(getApplication())
    fun skip()   = TimerForegroundService.skip(getApplication())
    fun stop()   = TimerForegroundService.stop(getApplication())

    override fun onCleared() {
        super.onCleared()
        TimerForegroundService.stop(getApplication())
        app.activePhase.value = null
    }
}

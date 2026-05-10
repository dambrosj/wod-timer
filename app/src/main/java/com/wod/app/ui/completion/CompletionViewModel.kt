package com.wod.app.ui.completion

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wod.app.WodApp
import com.wod.app.domain.model.WorkoutLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CompletionViewModel(app: Application) : AndroidViewModel(app) {

    private val wodApp = app as WodApp

    /** The workout that just finished — populated by TimerForegroundService on completion. */
    val workoutLog: WorkoutLog? get() = wodApp.pendingWorkoutLog

    /** Save with optional notes, clear pending log, then invoke [onDone] on the main thread. */
    fun save(notes: String, onDone: () -> Unit) {
        val log = workoutLog ?: run { onDone(); return }
        viewModelScope.launch {
            wodApp.workoutRepository.log(log.copy(notes = notes.trim()))
            wodApp.pendingWorkoutLog = null
            withContext(Dispatchers.Main) { onDone() }
        }
    }

    fun buildShareText(): String {
        val log = workoutLog ?: return "Ho completato un allenamento con WOD App! 💪"
        val dur = formatDuration(log.durationSeconds)
        return "Ho appena completato un allenamento ${log.type.name} di $dur con WOD App! 💪"
    }

    private fun formatDuration(totalSeconds: Int): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return if (h > 0) "${h}h ${m}m ${s}s" else if (m > 0) "${m}m ${s}s" else "${s}s"
    }
}

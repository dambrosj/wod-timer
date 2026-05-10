package com.wod.app.ui.diary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wod.app.WodApp
import com.wod.app.domain.model.WorkoutLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiaryDetailViewModel(app: Application) : AndroidViewModel(app) {

    private val wodApp = app as WodApp

    /** Log selected before navigating here; populated by DiaryViewModel.selectLog(). */
    val log: WorkoutLog? get() = wodApp.selectedWorkoutLog

    fun saveNotes(notes: String, onDone: () -> Unit) {
        val id = log?.id ?: run { onDone(); return }
        viewModelScope.launch {
            wodApp.workoutRepository.updateNotes(id, notes.trim())
            withContext(Dispatchers.Main) { onDone() }
        }
    }

    fun delete(onDone: () -> Unit) {
        val id = log?.id ?: run { onDone(); return }
        viewModelScope.launch {
            wodApp.workoutRepository.delete(id)
            wodApp.selectedWorkoutLog = null
            withContext(Dispatchers.Main) { onDone() }
        }
    }
}

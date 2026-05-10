package com.wod.app.ui.diary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wod.app.WodApp
import com.wod.app.domain.model.WorkoutLog
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DiaryViewModel(app: Application) : AndroidViewModel(app) {

    private val wodApp = app as WodApp

    val history: StateFlow<List<WorkoutLog>> = wodApp.workoutRepository.historyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectLog(log: WorkoutLog) {
        wodApp.selectedWorkoutLog = log
    }

    fun delete(id: Long) {
        viewModelScope.launch { wodApp.workoutRepository.delete(id) }
    }
}

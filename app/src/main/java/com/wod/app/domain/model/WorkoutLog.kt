package com.wod.app.domain.model

/** Domain representation of a completed workout — PRD §4. */
data class WorkoutLog(
    val id: Long = 0,
    val type: TimerType,
    val configJson: String,
    val durationSeconds: Int,
    val completedAt: Long,
    val notes: String = "",
    val savedWodId: String? = null,
)

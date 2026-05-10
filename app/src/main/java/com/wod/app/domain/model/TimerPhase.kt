package com.wod.app.domain.model

enum class PhaseType { WORK, REST, COUNTDOWN, WOD_REST }

/**
 * Snapshot of the engine state, emitted every second to the UI — PRD §4.
 *
 * [currentExercise] / [nextExercise] are populated by the engine according
 * to the resolution rule: present-only-during-WORK for current, and during
 * REST/COUNTDOWN/WOD_REST for next. Either may be null if the config has
 * no exercises list.
 */
data class TimerPhase(
    val label: String,
    val currentRound: Int,
    val totalRounds: Int,
    val currentWodRound: Int,
    val totalWodRounds: Int,
    val remainingSeconds: Int,
    val totalSeconds: Int,
    val phase: PhaseType,
    val currentExercise: String? = null,
    val nextExercise: String? = null,
    val isPaused: Boolean = false,
)

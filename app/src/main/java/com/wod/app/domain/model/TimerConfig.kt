package com.wod.app.domain.model

import kotlinx.serialization.Serializable

/**
 * WOD-repetition block shared by every timer type — PRD §4.
 *
 * The whole timer can be repeated [wodRounds] times with
 * [restBetweenRoundsSeconds] of rest between repetitions. Setting
 * `wodRounds = 1` (the default) means no repetition.
 */
@Serializable
data class WodRepeatConfig(
    val wodRounds: Int = 1,
    val restBetweenRoundsSeconds: Int = 0,
)

/**
 * One TABATA block. A config may contain a single block (the "main"
 * `series/work/rest` triplet) or many via [sets] for circuit-style use.
 *
 * Per-series exercise names live in [exercises]. Resolution rule
 * (PRD §4 / TimerPhase): empty list → no label; size 1 → same exercise
 * for every series; size == series → 1:1 mapping; otherwise cyclic.
 */
@Serializable
sealed class TimerConfig {

    abstract val notes: String
    abstract val repeat: WodRepeatConfig

    @Serializable
    data class Amrap(
        val durationSeconds: Int,
        val exercises: List<String> = emptyList(),
        /** Target reps per exercise (parallel to [exercises]; 0 = not set). */
        val exerciseReps: List<Int> = emptyList(),
        /** Minimum reps that must be logged before the athlete is allowed to pause. 0 = no constraint. */
        val exerciseMinReps: List<Int> = emptyList(),
        override val notes: String = "",
        override val repeat: WodRepeatConfig = WodRepeatConfig(),
    ) : TimerConfig()

    @Serializable
    data class ForTime(
        val timecapSeconds: Int,
        val rounds: Int = 1,
        val exercises: List<String> = emptyList(),
        /** Target reps per exercise (parallel to [exercises]; 0 = not set). */
        val exerciseReps: List<Int> = emptyList(),
        /** Minimum reps that must be logged before the athlete is allowed to pause. 0 = no constraint. */
        val exerciseMinReps: List<Int> = emptyList(),
        override val notes: String = "",
        override val repeat: WodRepeatConfig = WodRepeatConfig(),
    ) : TimerConfig()

    @Serializable
    data class Emom(
        val totalMinutes: Int,
        val intervalSeconds: Int = 60,
        val exercises: List<String> = emptyList(),
        override val notes: String = "",
        override val repeat: WodRepeatConfig = WodRepeatConfig(),
    ) : TimerConfig()

    @Serializable
    data class Tabata(
        val series: Int,
        val workSeconds: Int,
        val restSeconds: Int,
        val sets: List<TabataSet> = emptyList(),
        val exercises: List<String> = emptyList(),
        override val notes: String = "",
        override val repeat: WodRepeatConfig = WodRepeatConfig(),
    ) : TimerConfig()

    @Serializable
    data class Mix(
        val segments: List<MixSegment>,
        override val notes: String = "",
        override val repeat: WodRepeatConfig = WodRepeatConfig(),
    ) : TimerConfig()

    /**
     * CUSTOM timer: a free-form ordered list of named intervals, each with its own duration.
     * Interval names are whatever the athlete wants ("PUSHUP", "RIPOSO", "CURL", …).
     * Optional WOD repetition via [repeat] (same mechanics as every other type).
     */
    @Serializable
    data class Custom(
        val intervals: List<CustomInterval> = emptyList(),
        override val notes: String = "",
        override val repeat: WodRepeatConfig = WodRepeatConfig(),
    ) : TimerConfig()
}

@Serializable
data class TabataSet(
    val series: Int,
    val workSeconds: Int,
    val restSeconds: Int,
)

/**
 * A segment inside a MIX timer. The [configJson] embeds one of the other
 * TimerConfig variants serialized to JSON, picked by [type].
 */
@Serializable
data class MixSegment(
    val type: TimerType,
    val configJson: String,
)

/**
 * One named step inside a CUSTOM timer.
 *
 * [name] is whatever the athlete calls it ("PUSHUP", "RIPOSO", "CURL", …).
 * [durationSeconds] is the length of that step.
 */
@Serializable
data class CustomInterval(
    val name: String = "",
    val durationSeconds: Int = 30,
)

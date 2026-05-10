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
        override val notes: String = "",
        override val repeat: WodRepeatConfig = WodRepeatConfig(),
    ) : TimerConfig()

    @Serializable
    data class ForTime(
        val timecapSeconds: Int,
        val rounds: Int = 1,
        val exercises: List<String> = emptyList(),
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

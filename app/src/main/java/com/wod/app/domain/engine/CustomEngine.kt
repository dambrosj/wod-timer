package com.wod.app.domain.engine

import com.wod.app.domain.model.PhaseType
import com.wod.app.domain.model.TimerConfig
import com.wod.app.domain.model.TimerPhase
import kotlinx.coroutines.CoroutineScope

/**
 * CUSTOM timer engine: plays a free-form ordered list of named intervals in sequence,
 * with optional WOD repetitions between complete passes of the list.
 *
 * Each interval maps to a single WORK phase whose [TimerPhase.label] is the interval name
 * (so the athlete sees "PUSHUP", "RIPOSO", "CURL", … as the big phase header).
 * [TimerPhase.currentExercise] is intentionally null during WORK to avoid duplication,
 * since the label already carries the name.
 *
 * Countdown (10 s) is injected before every WOD round, same as every other engine.
 */
class CustomEngine(
    private val config: TimerConfig.Custom,
    scope: CoroutineScope,
) : BaseIntervalEngine(scope) {

    // ── Plan ─────────────────────────────────────────────────────────────────

    override fun buildPlan(): List<PhaseStep> {
        val intervals = config.intervals.filter { it.durationSeconds > 0 }
        if (intervals.isEmpty()) return emptyList()

        val steps = mutableListOf<PhaseStep>()
        val wodRounds = config.repeat.wodRounds.coerceAtLeast(1)

        for (wodRound in 1..wodRounds) {
            steps += PhaseStep(PhaseType.COUNTDOWN, COUNTDOWN_SECONDS, 0, wodRound)
            intervals.forEachIndexed { index, interval ->
                steps += PhaseStep(
                    phaseType       = PhaseType.WORK,
                    durationSeconds = interval.durationSeconds,
                    seriesIndex     = index,
                    wodRound        = wodRound,
                )
            }
            if (wodRound < wodRounds && config.repeat.restBetweenRoundsSeconds > 0) {
                steps += PhaseStep(
                    phaseType       = PhaseType.WOD_REST,
                    durationSeconds = config.repeat.restBetweenRoundsSeconds,
                    seriesIndex     = intervals.size - 1,
                    wodRound        = wodRound,
                )
            }
        }
        return steps
    }

    // ── Phase snapshot ────────────────────────────────────────────────────────

    override fun makePhase(step: PhaseStep, planIndex: Int, remaining: Int): TimerPhase {
        val intervals = config.intervals.filter { it.durationSeconds > 0 }
        val interval = intervals.getOrNull(step.seriesIndex)

        // The next WORK step after this one (used for next-exercise preview).
        val nextWorkStep = plan.drop(planIndex + 1).firstOrNull { it.phaseType == PhaseType.WORK }
        val nextName = nextWorkStep?.let { intervals.getOrNull(it.seriesIndex)?.name }
            ?.takeIf { it.isNotBlank() }

        return TimerPhase(
            label = when (step.phaseType) {
                // Interval name is the phase label — the athlete reads it as the big header.
                PhaseType.WORK      -> interval?.name?.takeIf { it.isNotBlank() }
                                       ?: "Intervallo ${step.seriesIndex + 1}"
                PhaseType.COUNTDOWN -> "Pronti"
                PhaseType.WOD_REST  -> "Riposo WOD"
                PhaseType.REST      -> "Riposo"
            },
            currentRound    = step.seriesIndex + 1,
            totalRounds     = intervals.size,
            currentWodRound = step.wodRound,
            totalWodRounds  = config.repeat.wodRounds.coerceAtLeast(1),
            remainingSeconds = remaining,
            totalSeconds    = step.durationSeconds,
            phase           = step.phaseType,
            // During COUNTDOWN: show the first interval name as an exercise preview.
            // During WORK: label IS the name — currentExercise is null to avoid duplication.
            currentExercise = when (step.phaseType) {
                PhaseType.COUNTDOWN -> intervals.firstOrNull()?.name?.takeIf { it.isNotBlank() }
                else                -> null
            },
            // CUSTOM: show next interval during WORK too (label IS the current name).
            // All other engines keep nextExercise = null during WORK so they are unaffected.
            nextExercise = nextName,
            isPaused = isPaused,
        )
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    companion object {
        /**
         * Total workout duration in seconds (excluding countdown).
         * Useful for the "Tempo totale" label on the config screen.
         */
        fun totalSeconds(config: TimerConfig.Custom): Int {
            val rounds = config.repeat.wodRounds.coerceAtLeast(1)
            val perRound = config.intervals.sumOf { it.durationSeconds.coerceAtLeast(0) }
            val betweenRests = (rounds - 1).coerceAtLeast(0) * config.repeat.restBetweenRoundsSeconds
            return perRound * rounds + betweenRests
        }
    }
}

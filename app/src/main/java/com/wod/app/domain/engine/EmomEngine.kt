package com.wod.app.domain.engine

import com.wod.app.domain.model.PhaseType
import com.wod.app.domain.model.TimerConfig
import com.wod.app.domain.model.TimerPhase
import kotlinx.coroutines.CoroutineScope

/**
 * EMOM engine: repeating interval timer, one WORK phase per interval (PRD T12).
 *
 * Plan per WOD round:
 *   WORK (intervalSeconds) × N intervals   where N = totalMinutes * 60 / intervalSeconds
 *   [WOD_REST if restBetweenRoundsSeconds > 0]
 *
 * The EMOM model: each interval is the full 60 s (or custom interval). The athlete
 * performs exercises at the start of each interval and rests for whatever time remains
 * until the next minute — this is tracked by the countdown, not a separate REST phase.
 *
 * Exercises cycle through [config.exercises] per interval via resolution rule:
 *   empty → no label; size 1 → same each interval; size N → 1:1; otherwise cyclic.
 */
class EmomEngine(
    private val config: TimerConfig.Emom,
    scope: CoroutineScope,
) : BaseIntervalEngine(scope) {

    private val intervals: Int =
        ((config.totalMinutes * 60) / config.intervalSeconds).coerceAtLeast(1)

    override fun buildPlan(): List<PhaseStep> {
        val steps = mutableListOf<PhaseStep>()
        val wodRounds = config.repeat.wodRounds.coerceAtLeast(1)
        for (r in 1..wodRounds) {
            steps += PhaseStep(PhaseType.COUNTDOWN, COUNTDOWN_SECONDS, 0, r)
            for (i in 0 until intervals) {
                steps += PhaseStep(PhaseType.WORK, config.intervalSeconds, i, r)
            }
            if (r < wodRounds && config.repeat.restBetweenRoundsSeconds > 0) {
                steps += PhaseStep(
                    PhaseType.WOD_REST,
                    config.repeat.restBetweenRoundsSeconds,
                    intervals - 1,
                    r,
                )
            }
        }
        return steps
    }

    override fun makePhase(step: PhaseStep, planIndex: Int, remaining: Int): TimerPhase {
        val nextStep = plan.drop(planIndex + 1).firstOrNull { it.phaseType == PhaseType.WORK }
        return TimerPhase(
            label = if (step.phaseType == PhaseType.WORK) "EMOM" else "Riposo WOD",
            currentRound = step.seriesIndex + 1,
            totalRounds = intervals,
            currentWodRound = step.wodRound,
            totalWodRounds = config.repeat.wodRounds.coerceAtLeast(1),
            remainingSeconds = remaining,
            totalSeconds = step.durationSeconds,
            phase = step.phaseType,
            currentExercise = resolveExercise(step.seriesIndex),
            nextExercise = if (step.phaseType == PhaseType.WORK) null
                           else nextStep?.let { resolveExercise(it.seriesIndex) },
            isPaused = isPaused,
        )
    }

    private fun resolveExercise(index: Int): String? {
        val ex = config.exercises
        return if (ex.isEmpty()) null else ex[index % ex.size]
    }

    companion object {
        fun totalSeconds(config: TimerConfig.Emom): Int {
            val intervals = ((config.totalMinutes * 60) / config.intervalSeconds).coerceAtLeast(1)
            val wodRounds = config.repeat.wodRounds.coerceAtLeast(1)
            val rest = (wodRounds - 1).coerceAtLeast(0) * config.repeat.restBetweenRoundsSeconds
            return config.intervalSeconds * intervals * wodRounds + rest
        }
    }
}

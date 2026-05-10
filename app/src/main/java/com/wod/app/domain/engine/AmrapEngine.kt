package com.wod.app.domain.engine

import com.wod.app.domain.model.PhaseType
import com.wod.app.domain.model.TimerConfig
import com.wod.app.domain.model.TimerPhase
import kotlinx.coroutines.CoroutineScope

/**
 * AMRAP engine: single countdown per WOD round, supports WOD repetitions (PRD T10).
 *
 * Plan per WOD round:
 *   WORK (durationSeconds)
 *   [WOD_REST if restBetweenRoundsSeconds > 0]
 *
 * There is no automatic REST between rounds — AMRAP is one continuous work block.
 * skip() advances to the next WOD round (or completes the workout on the last round).
 *
 * The [TimerPhase.remainingSeconds] field counts down; elapsed = durationSeconds - remaining.
 * Exercises are shown as a checklist in the UI (not phase-switched by the engine).
 */
class AmrapEngine(
    private val config: TimerConfig.Amrap,
    scope: CoroutineScope,
) : BaseIntervalEngine(scope) {

    override fun buildPlan(): List<PhaseStep> {
        val steps = mutableListOf<PhaseStep>()
        val wodRounds = config.repeat.wodRounds.coerceAtLeast(1)
        for (r in 1..wodRounds) {
            steps += PhaseStep(PhaseType.COUNTDOWN, COUNTDOWN_SECONDS, 0, r)
            steps += PhaseStep(PhaseType.WORK, config.durationSeconds, 0, r)
            if (r < wodRounds && config.repeat.restBetweenRoundsSeconds > 0) {
                steps += PhaseStep(
                    PhaseType.WOD_REST,
                    config.repeat.restBetweenRoundsSeconds,
                    0,
                    r,
                )
            }
        }
        return steps
    }

    override fun makePhase(step: PhaseStep, planIndex: Int, remaining: Int): TimerPhase =
        TimerPhase(
            label = if (step.phaseType == PhaseType.WORK) "AMRAP" else "Riposo WOD",
            currentRound = 1,
            totalRounds = 1,
            currentWodRound = step.wodRound,
            totalWodRounds = config.repeat.wodRounds.coerceAtLeast(1),
            remainingSeconds = remaining,
            totalSeconds = step.durationSeconds,
            phase = step.phaseType,
            currentExercise = if (step.phaseType == PhaseType.WORK) resolveExercise() else null,
            nextExercise = null,
            isPaused = isPaused,
        )

    private fun resolveExercise(): String? {
        val ex = config.exercises
        return if (ex.isEmpty()) null else ex[0]
    }

    companion object {
        /** Total workout duration in seconds for the config screen label. */
        fun totalSeconds(config: TimerConfig.Amrap): Int {
            val rounds = config.repeat.wodRounds.coerceAtLeast(1)
            val rest = (rounds - 1).coerceAtLeast(0) * config.repeat.restBetweenRoundsSeconds
            return config.durationSeconds * rounds + rest
        }
    }
}

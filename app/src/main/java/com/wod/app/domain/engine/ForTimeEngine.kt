package com.wod.app.domain.engine

import com.wod.app.domain.model.PhaseType
import com.wod.app.domain.model.TimerConfig
import com.wod.app.domain.model.TimerPhase
import kotlinx.coroutines.CoroutineScope

/**
 * FOR TIME engine: timecap countdown, manual completion via skip() (PRD T11).
 *
 * Plan per WOD round:
 *   WORK (timecapSeconds)  — counts down; user taps skip() when done early
 *   [WOD_REST if restBetweenRoundsSeconds > 0]
 *
 * skip() = "I'm done with this round" → advances to the next WOD round
 *          (or completes the workout on the last round).
 * When the timecap reaches 0, the engine also advances automatically.
 *
 * [TimerPhase.totalRounds] carries [TimerConfig.ForTime.rounds] so the UI
 * can display "Round X / Y" even though the engine does not auto-advance rounds.
 */
class ForTimeEngine(
    private val config: TimerConfig.ForTime,
    scope: CoroutineScope,
) : BaseIntervalEngine(scope) {

    override fun buildPlan(): List<PhaseStep> {
        val steps = mutableListOf<PhaseStep>()
        val wodRounds = config.repeat.wodRounds.coerceAtLeast(1)
        for (r in 1..wodRounds) {
            steps += PhaseStep(PhaseType.COUNTDOWN, COUNTDOWN_SECONDS, 0, r)
            steps += PhaseStep(PhaseType.WORK, config.timecapSeconds, 0, r)
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
            label = if (step.phaseType == PhaseType.WORK) "FOR TIME" else "Riposo WOD",
            currentRound = 1,
            totalRounds = config.rounds.coerceAtLeast(1),
            currentWodRound = step.wodRound,
            totalWodRounds = config.repeat.wodRounds.coerceAtLeast(1),
            remainingSeconds = remaining,
            totalSeconds = step.durationSeconds,
            phase = step.phaseType,
            currentExercise = resolveExercise(0),
            nextExercise = null,
            isPaused = isPaused,
        )

    private fun resolveExercise(roundIndex: Int): String? {
        val ex = config.exercises
        return if (ex.isEmpty()) null else ex[roundIndex % ex.size]
    }

    companion object {
        fun totalSeconds(config: TimerConfig.ForTime): Int {
            val rounds = config.repeat.wodRounds.coerceAtLeast(1)
            val rest = (rounds - 1).coerceAtLeast(0) * config.repeat.restBetweenRoundsSeconds
            return config.timecapSeconds * rounds + rest
        }
    }
}

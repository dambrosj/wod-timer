package com.wod.app.domain.engine

import com.wod.app.domain.model.PhaseType
import com.wod.app.domain.model.TimerConfig
import com.wod.app.domain.model.TimerPhase
import kotlinx.coroutines.CoroutineScope

/**
 * Full TABATA engine: coroutine-driven, 1-second tick, supports WOD repetitions,
 * pause/resume/skip and AudioCue emission per PRD §7 + §7.bis.
 *
 * [scope] is the caller's lifecycle scope (typically TimerForegroundService).
 */
class TabataEngine(
    private val config: TimerConfig.Tabata,
    scope: CoroutineScope,
) : BaseIntervalEngine(scope) {

    // ── Plan ─────────────────────────────────────────────────────────────────

    override fun buildPlan(): List<PhaseStep> {
        val steps = mutableListOf<PhaseStep>()
        val wodRounds = config.repeat.wodRounds.coerceAtLeast(1)

        for (wodRound in 1..wodRounds) {
            // 10-second countdown before every WOD round (T32)
            steps += PhaseStep(PhaseType.COUNTDOWN, COUNTDOWN_SECONDS, 0, wodRound)
            for (seriesIndex in 0 until config.series) {
                steps += PhaseStep(PhaseType.WORK, config.workSeconds, seriesIndex, wodRound)
                // No REST after the last series of each round: the round ends cleanly,
                // followed by WOD_REST (if more rounds remain) or workout complete.
                if (seriesIndex < config.series - 1) {
                    steps += PhaseStep(PhaseType.REST, config.restSeconds, seriesIndex, wodRound)
                }
            }
            if (wodRound < wodRounds && config.repeat.restBetweenRoundsSeconds > 0) {
                steps += PhaseStep(
                    PhaseType.WOD_REST,
                    config.repeat.restBetweenRoundsSeconds,
                    config.series - 1,
                    wodRound,
                )
            }
        }
        return steps
    }

    // ── Phase snapshot ────────────────────────────────────────────────────────

    override fun makePhase(step: PhaseStep, planIndex: Int, remaining: Int): TimerPhase {
        val nextWorkStep = plan.drop(planIndex + 1).firstOrNull { it.phaseType == PhaseType.WORK }
        return TimerPhase(
            label = phaseLabel(step.phaseType),
            currentRound = step.seriesIndex + 1,
            totalRounds = config.series,
            currentWodRound = step.wodRound,
            totalWodRounds = config.repeat.wodRounds.coerceAtLeast(1),
            remainingSeconds = remaining,
            totalSeconds = step.durationSeconds,
            phase = step.phaseType,
            currentExercise = resolveExercise(step.seriesIndex),
            nextExercise = if (step.phaseType == PhaseType.WORK) null
                           else nextWorkStep?.let { resolveExercise(it.seriesIndex) },
            isPaused = isPaused,
        )
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun resolveExercise(seriesIndex: Int): String? {
        val ex = config.exercises
        return if (ex.isEmpty()) null else ex[seriesIndex % ex.size]
    }

    private fun phaseLabel(type: PhaseType) = when (type) {
        PhaseType.WORK      -> "Lavoro"
        PhaseType.REST      -> "Riposo"
        PhaseType.WOD_REST  -> "Riposo WOD"
        PhaseType.COUNTDOWN -> "Pronti"
    }

    companion object {
        /**
         * Total workout duration in seconds — for the "Tempo totale" label on the config screen.
         *
         * Each round: N×work + (N-1)×rest  (no rest after the last series).
         * Between rounds: WOD_REST × (rounds-1).
         */
        fun totalSeconds(config: TimerConfig.Tabata): Int {
            val rounds = config.repeat.wodRounds.coerceAtLeast(1)
            val perRound = config.series * config.workSeconds +
                           (config.series - 1) * config.restSeconds
            val betweenRests = (rounds - 1).coerceAtLeast(0) * config.repeat.restBetweenRoundsSeconds
            return perRound * rounds + betweenRests
        }
    }
}


package com.wod.app.domain.engine

import com.wod.app.domain.model.AudioCue
import com.wod.app.domain.model.PhaseType
import com.wod.app.domain.model.TimerPhase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Shared coroutine-driven engine loop used by all interval-timer types.
 *
 * Subclasses provide:
 *  - [buildPlan] — the ordered list of [PhaseStep]s representing the full workout.
 *  - [makePhase] — maps a (step, planIndex, remaining) triple to the [TimerPhase]
 *    snapshot exposed to the UI every second.
 *
 * The base class owns the command channel, the 1-second tick, pause/resume/skip,
 * cue emission (open to override), and workout completion signalling.
 */
abstract class BaseIntervalEngine(
    protected val scope: CoroutineScope,
) : TimerEngine {

    companion object {
        /** Duration of the pre-WOD-round countdown injected by all engines (T32). */
        const val COUNTDOWN_SECONDS = 10
    }

    /** One atomic step in the full workout sequence. */
    data class PhaseStep(
        val phaseType: PhaseType,
        val durationSeconds: Int,
        val seriesIndex: Int = 0,
        val wodRound: Int = 1,
    )

    /** Exposed as internal for unit tests; computed once via [buildPlan]. */
    internal val plan: List<PhaseStep> by lazy { buildPlan() }

    private val _phase: MutableStateFlow<TimerPhase> by lazy {
        MutableStateFlow(makePhase(plan[0], 0, plan[0].durationSeconds))
    }
    private val _cues = MutableSharedFlow<AudioCue>(extraBufferCapacity = 16)
    private val _completed = MutableStateFlow(false)

    override val phase: Flow<TimerPhase> get() = _phase
    override val cues: Flow<AudioCue> = _cues.asSharedFlow()
    override val isCompleted: Flow<Boolean> = _completed

    private enum class Cmd { Pause, Resume, Skip, Stop }

    private val cmdChannel = Channel<Cmd>(Channel.UNLIMITED)
    private var engineJob: Job? = null

    @Volatile protected var isPaused = false
        private set

    protected abstract fun buildPlan(): List<PhaseStep>
    protected abstract fun makePhase(step: PhaseStep, planIndex: Int, remaining: Int): TimerPhase

    /** Emit a cue from within a suspend context (e.g. an overriding [emitCues]). */
    protected suspend fun emitCue(cue: AudioCue) = _cues.emit(cue)

    override fun start() {
        if (engineJob?.isActive == true) return
        engineJob = scope.launch { runEngine() }
    }

    override fun pause()  { cmdChannel.trySend(Cmd.Pause) }
    override fun resume() { cmdChannel.trySend(Cmd.Resume) }
    override fun skip()   { cmdChannel.trySend(Cmd.Skip) }

    override fun stop() {
        cmdChannel.trySend(Cmd.Stop)
        engineJob?.cancel()
        engineJob = null
    }

    // ── Engine loop ───────────────────────────────────────────────────────────

    private suspend fun runEngine() {
        var planIndex = 0

        while (planIndex < plan.size) {
            val step = plan[planIndex]
            var remaining = step.durationSeconds
            publishPhase(step, planIndex, remaining)

            var skipThisPhase = false

            while (remaining > 0 && !skipThisPhase) {
                val cmd = withTimeoutOrNull(1_000L) { cmdChannel.receive() }

                when (cmd) {
                    Cmd.Pause -> {
                        isPaused = true
                        publishPhase(step, planIndex, remaining)
                        var waitingForResume = true
                        while (waitingForResume) {
                            when (cmdChannel.receive()) {
                                Cmd.Resume -> waitingForResume = false
                                Cmd.Skip   -> { waitingForResume = false; skipThisPhase = true }
                                Cmd.Stop   -> return
                                Cmd.Pause  -> {}
                            }
                        }
                        isPaused = false
                        publishPhase(step, planIndex, remaining)
                    }

                    Cmd.Skip   -> skipThisPhase = true
                    Cmd.Stop   -> return
                    Cmd.Resume -> {} // spurious resume — ignore

                    null -> {
                        // 1 second elapsed
                        remaining--
                        emitCues(step, remaining)
                        publishPhase(step, planIndex, remaining)
                    }
                }
            }

            // Advance to next phase
            planIndex++
            if (planIndex < plan.size) {
                val next = plan[planIndex]
                _cues.emit(AudioCue.PhaseTransition(next.phaseType))
                if (next.phaseType == PhaseType.WORK && next.wodRound > step.wodRound) {
                    _cues.emit(AudioCue.WodRoundComplete)
                }
            }
        }

        _completed.value = true
        _cues.emit(AudioCue.WorkoutComplete)
    }

    // ── Cue emission ──────────────────────────────────────────────────────────

    /**
     * Emits the appropriate [AudioCue]s for the given step and [remaining] seconds.
     * Subclasses may override to add engine-specific cues.
     */
    protected open suspend fun emitCues(step: PhaseStep, remaining: Int) {
        // Last-3 countdown cues for every phase type.
        if (remaining in 1..3) {
            _cues.emit(
                when (step.phaseType) {
                    PhaseType.WORK      -> AudioCue.WorkLast3(remaining)
                    PhaseType.REST      -> AudioCue.RestLast3(remaining)
                    PhaseType.WOD_REST  -> AudioCue.WodRestLast3(remaining)
                    PhaseType.COUNTDOWN -> AudioCue.CountdownTick(remaining)
                }
            )
        }
        // Halfway cue — only for WORK phases longer than 6 s (PRD §7.bis).
        if (step.phaseType == PhaseType.WORK && step.durationSeconds > 6) {
            val halfway = step.durationSeconds / 2
            if (remaining == halfway) _cues.emit(AudioCue.WorkHalfway)
        }
    }

    private fun publishPhase(step: PhaseStep, planIndex: Int, remaining: Int) {
        _phase.value = makePhase(step, planIndex, remaining)
    }
}

package com.wod.app.domain.engine

import com.wod.app.domain.model.AudioCue
import com.wod.app.domain.model.TimerPhase
import kotlinx.coroutines.flow.Flow

/**
 * Common contract for AMRAP / FOR TIME / EMOM / TABATA / MIX engines.
 *
 * Implementations are responsible for:
 *  - sequencing phases according to their config (incl. WOD repetitions)
 *  - emitting one [TimerPhase] snapshot per second on [phase]
 *  - emitting [AudioCue] events at the precise tick boundaries
 *    (last 3s of work/rest, halfway of work, phase transitions, completion)
 *
 * The engine itself does NOT play sounds, hold WakeLocks or post
 * notifications — those are concerns of [com.wod.app.service.TimerForegroundService].
 *
 * Skip semantics (PRD §7):
 *  - skip from WORK   → next REST
 *  - skip from REST   → next WORK (or WOD_REST if last series of round)
 *  - skip from WOD_REST → next round's first WORK
 *  - skip from final WORK of final round → workout complete
 */
interface TimerEngine {

    val phase: Flow<TimerPhase>
    val cues: Flow<AudioCue>
    val isCompleted: Flow<Boolean>

    fun start()
    fun pause()
    fun resume()
    fun skip()
    fun stop()
}

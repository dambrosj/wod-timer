package com.wod.app.domain.model

/** Audio cue categories, used to gate playback per user setting — PRD §7.bis. */
enum class CueCategory { COUNTDOWN, HALFWAY, PHASE_TRANSITION, COMPLETION }

/**
 * Audio events emitted by every TimerEngine. The engine itself is
 * audio-agnostic: it only knows about phases and seconds. Playback
 * logic lives in [com.wod.app.data.audio.AudioCueManager].
 */
sealed class AudioCue(val category: CueCategory) {
    data class CountdownTick(val n: Int) : AudioCue(CueCategory.COUNTDOWN)
    data object WorkHalfway : AudioCue(CueCategory.HALFWAY)
    data class WorkLast3(val n: Int) : AudioCue(CueCategory.COUNTDOWN)
    data class RestLast3(val n: Int) : AudioCue(CueCategory.COUNTDOWN)
    data class WodRestLast3(val n: Int) : AudioCue(CueCategory.COUNTDOWN)
    data object WodRoundComplete : AudioCue(CueCategory.PHASE_TRANSITION)
    data class PhaseTransition(val toPhase: PhaseType) : AudioCue(CueCategory.PHASE_TRANSITION)
    data object WorkoutComplete : AudioCue(CueCategory.COMPLETION)
}

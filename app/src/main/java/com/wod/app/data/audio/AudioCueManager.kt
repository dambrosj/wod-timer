package com.wod.app.data.audio

import com.wod.app.domain.model.AudioCue
import com.wod.app.domain.model.CueCategory

/**
 * Plays the audio cues defined in PRD §7.bis.
 *
 * Implementation notes (for the SoundPool-backed impl):
 *  - 4 short samples in res/raw: cue_tick, cue_halfway, cue_phase, cue_complete
 *  - SoundPool built with AudioAttributes.USAGE_ALARM so the device's media
 *    volume slider does NOT silence our cues; instead the in-app [setVolume]
 *    is the single source of truth (PRD requirement).
 */
interface AudioCueManager {
    fun preload()
    fun play(cue: AudioCue)
    fun playTestSequence()

    /**
     * Plays a single tick sound at the current volume.
     * Used by the Settings volume slider to give immediate auditory feedback
     * when the user releases the slider.
     */
    fun playPreview()

    fun setMasterEnabled(enabled: Boolean)
    fun setCategoryEnabled(category: CueCategory, enabled: Boolean)
    fun setVolume(volume: Float)

    fun release()
}

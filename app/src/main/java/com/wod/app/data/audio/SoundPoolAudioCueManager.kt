package com.wod.app.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.wod.app.R
import com.wod.app.domain.model.AudioCue
import com.wod.app.domain.model.CueCategory
import java.util.concurrent.atomic.AtomicBoolean

/**
 * SoundPool implementation of [AudioCueManager].
 *
 * Four samples from res/raw are loaded via [preload]:
 * cue_tick, cue_halfway, cue_phase, cue_complete.
 *
 * Volume decoupling (PRD §7.bis):
 *  - SoundPool is created with USAGE_ALARM so the system "Media" slider
 *    doesn't silence cues.
 *  - The per-call leftVolume/rightVolume carry the user's in-app slider
 *    value (0f..1f), the single source of truth for in-app volume.
 */
class SoundPoolAudioCueManager(
    private val context: Context,
) : AudioCueManager {

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                // USAGE_GAME routes to the media stream (same as Spotify):
                // - does NOT request audio focus → Spotify keeps playing, never paused
                // - volume scale is identical to music → at 1.0f cues are loud over music
                // - our in-app slider (0f..1f) gives independent control on top of media volume
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val sampleIds = mutableMapOf<Sample, Int>()
    private val loaded = AtomicBoolean(false)

    @Volatile private var masterEnabled: Boolean = true
    @Volatile private var volume: Float = 1f
    private val categoryEnabled = mutableMapOf(
        CueCategory.COUNTDOWN to true,
        CueCategory.HALFWAY to true,
        CueCategory.PHASE_TRANSITION to true,
        CueCategory.COMPLETION to true,
    )

    override fun preload() {
        if (loaded.getAndSet(true)) return
        sampleIds[Sample.TICK]     = pool.load(context, R.raw.cue_tick, 1)
        sampleIds[Sample.HALFWAY]  = pool.load(context, R.raw.cue_halfway, 1)
        sampleIds[Sample.PHASE]    = pool.load(context, R.raw.cue_phase, 1)
        sampleIds[Sample.COMPLETE] = pool.load(context, R.raw.cue_complete, 1)
    }

    override fun play(cue: AudioCue) {
        if (!masterEnabled) return
        if (categoryEnabled[cue.category] != true) return
        val sample = sampleFor(cue) ?: return
        val id = sampleIds[sample] ?: return
        pool.play(id, volume, volume, /* priority */ 1, /* loop */ 0, /* rate */ 1f)
    }

    override fun playPreview() {
        if (!masterEnabled) return
        val id = sampleIds[Sample.TICK] ?: return
        pool.play(id, volume, volume, /* priority */ 1, /* loop */ 0, /* rate */ 1f)
    }

    override fun playTestSequence() {
        if (!masterEnabled) return
        val order = listOf(Sample.TICK, Sample.HALFWAY, Sample.PHASE, Sample.COMPLETE)
        Thread {
            order.forEach { sample ->
                val id = sampleIds[sample] ?: return@forEach
                pool.play(id, volume, volume, 1, 0, 1f)
                Thread.sleep(350)
            }
        }.start()
    }

    override fun setMasterEnabled(enabled: Boolean) { masterEnabled = enabled }
    override fun setCategoryEnabled(category: CueCategory, enabled: Boolean) {
        categoryEnabled[category] = enabled
    }
    override fun setVolume(volume: Float) { this.volume = volume.coerceIn(0f, 1f) }

    override fun release() {
        pool.release()
        sampleIds.clear()
        loaded.set(false)
    }

    private fun sampleFor(cue: AudioCue): Sample? = when (cue) {
        is AudioCue.CountdownTick,
        is AudioCue.WorkLast3,
        is AudioCue.RestLast3,
        is AudioCue.WodRestLast3        -> Sample.TICK
        is AudioCue.WorkHalfway         -> Sample.HALFWAY
        is AudioCue.PhaseTransition,
        is AudioCue.WodRoundComplete    -> Sample.PHASE
        is AudioCue.WorkoutComplete     -> Sample.COMPLETE
    }

    private enum class Sample { TICK, HALFWAY, PHASE, COMPLETE }
}

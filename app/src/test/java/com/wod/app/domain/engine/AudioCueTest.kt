package com.wod.app.domain.engine

import com.wod.app.domain.model.AudioCue
import com.wod.app.domain.model.CueCategory
import com.wod.app.domain.model.PhaseType
import com.wod.app.domain.model.TimerConfig
import com.wod.app.domain.model.WodRepeatConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for audio cue emission logic (T51).
 *
 * Uses UnconfinedTestDispatcher so all coroutines run eagerly on the test
 * thread, and advanceTimeBy controls the virtual 1-second tick in the engine.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AudioCueTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private fun tabata(
        series: Int = 1,
        workSeconds: Int = 8,
        restSeconds: Int = 4,
    ) = TimerConfig.Tabata(
        series = series,
        workSeconds = workSeconds,
        restSeconds = restSeconds,
        repeat = WodRepeatConfig(),
    )

    private suspend fun kotlinx.coroutines.test.TestScope.collectCues(
        engine: TabataEngine,
        totalTicks: Int,
    ): List<AudioCue> {
        val cues = mutableListOf<AudioCue>()
        val job = launch(testDispatcher) { engine.cues.collect { cues.add(it) } }
        engine.start()
        advanceTimeBy(totalTicks * 1_000L + 500L)
        job.cancel()
        engine.stop()
        return cues
    }

    @Test
    fun workPhaseEmitsWorkLast3() = runTest(testDispatcher) {
        val engine = TabataEngine(tabata(series = 1, workSeconds = 6, restSeconds = 2), this)
        val cues = collectCues(engine, 19)
        val workLast3 = cues.filterIsInstance<AudioCue.WorkLast3>()
        assertTrue("Should have WorkLast3(3)", workLast3.any { it.n == 3 })
        assertTrue("Should have WorkLast3(2)", workLast3.any { it.n == 2 })
        assertTrue("Should have WorkLast3(1)", workLast3.any { it.n == 1 })
    }

    @Test
    fun restPhaseEmitsRestLast3() = runTest(testDispatcher) {
        val engine = TabataEngine(tabata(series = 1, workSeconds = 4, restSeconds = 4), this)
        val cues = collectCues(engine, 20)
        val restLast3 = cues.filterIsInstance<AudioCue.RestLast3>()
        assertTrue("Should have RestLast3(3)", restLast3.any { it.n == 3 })
        assertTrue("Should have RestLast3(2)", restLast3.any { it.n == 2 })
        assertTrue("Should have RestLast3(1)", restLast3.any { it.n == 1 })
    }

    @Test
    fun countdownPhaseEmitsCountdownTick() = runTest(testDispatcher) {
        val engine = TabataEngine(tabata(series = 1, workSeconds = 4, restSeconds = 2), this)
        val cues = collectCues(engine, 11)
        val ticks = cues.filterIsInstance<AudioCue.CountdownTick>()
        assertTrue("Should have CountdownTick(3)", ticks.any { it.n == 3 })
        assertTrue("Should have CountdownTick(2)", ticks.any { it.n == 2 })
        assertTrue("Should have CountdownTick(1)", ticks.any { it.n == 1 })
    }

    @Test
    fun workHalfwaySuppressedWhenWorkIs6Seconds() = runTest(testDispatcher) {
        val engine = TabataEngine(tabata(series = 1, workSeconds = 6, restSeconds = 2), this)
        val cues = collectCues(engine, 20)
        assertFalse("WorkHalfway must be suppressed for <=6s", cues.any { it is AudioCue.WorkHalfway })
    }

    @Test
    fun workHalfwaySuppressedWhenWorkIs4Seconds() = runTest(testDispatcher) {
        val engine = TabataEngine(tabata(series = 1, workSeconds = 4, restSeconds = 2), this)
        val cues = collectCues(engine, 18)
        assertFalse(cues.any { it is AudioCue.WorkHalfway })
    }

    @Test
    fun workHalfwayEmittedOnceFor8sWorkPhase() = runTest(testDispatcher) {
        val engine = TabataEngine(tabata(series = 1, workSeconds = 8, restSeconds = 2), this)
        val cues = collectCues(engine, 22)
        val halfway = cues.filterIsInstance<AudioCue.WorkHalfway>()
        assertEquals("Should emit exactly 1 WorkHalfway for 1 WORK phase > 6s", 1, halfway.size)
    }

    @Test
    fun workHalfwayEmittedOncePerWorkPhaseForMultipleSeries() = runTest(testDispatcher) {
        val engine = TabataEngine(tabata(series = 3, workSeconds = 10, restSeconds = 2), this)
        val cues = collectCues(engine, 50)
        val halfway = cues.filterIsInstance<AudioCue.WorkHalfway>()
        assertEquals("Should emit 3 WorkHalfway (one per WORK phase)", 3, halfway.size)
    }

    @Test
    fun phaseTransitionEmittedWhenEnteringWork() = runTest(testDispatcher) {
        val engine = TabataEngine(tabata(series = 1, workSeconds = 4, restSeconds = 2), this)
        val cues = collectCues(engine, 18)
        val transitions = cues.filterIsInstance<AudioCue.PhaseTransition>()
        assertTrue("PhaseTransition to WORK expected", transitions.any { it.toPhase == PhaseType.WORK })
    }

    @Test
    fun phaseTransitionEmittedWhenEnteringRest() = runTest(testDispatcher) {
        val engine = TabataEngine(tabata(series = 1, workSeconds = 4, restSeconds = 3), this)
        val cues = collectCues(engine, 19)
        val transitions = cues.filterIsInstance<AudioCue.PhaseTransition>()
        assertTrue("PhaseTransition to REST expected", transitions.any { it.toPhase == PhaseType.REST })
    }

    @Test
    fun workoutCompleteEmittedOnce() = runTest(testDispatcher) {
        val engine = TabataEngine(tabata(series = 1, workSeconds = 2, restSeconds = 1), this)
        val cues = collectCues(engine, 15)
        assertEquals(1, cues.count { it is AudioCue.WorkoutComplete })
    }

    @Test
    fun workHalfwayCategoryIsHalfway() {
        assertEquals(CueCategory.HALFWAY, AudioCue.WorkHalfway.category)
    }

    @Test
    fun countdownTickCategoryIsCountdown() {
        assertEquals(CueCategory.COUNTDOWN, AudioCue.CountdownTick(3).category)
    }

    @Test
    fun workLast3CategoryIsCountdown() {
        assertEquals(CueCategory.COUNTDOWN, AudioCue.WorkLast3(2).category)
    }

    @Test
    fun restLast3CategoryIsCountdown() {
        assertEquals(CueCategory.COUNTDOWN, AudioCue.RestLast3(1).category)
    }

    @Test
    fun phaseTransitionCategoryIsPhaseTransition() {
        assertEquals(CueCategory.PHASE_TRANSITION, AudioCue.PhaseTransition(PhaseType.WORK).category)
    }

    @Test
    fun workoutCompleteCategoryIsCompletion() {
        assertEquals(CueCategory.COMPLETION, AudioCue.WorkoutComplete.category)
    }

    @Test
    fun wodRoundCompleteCategoryIsPhaseTransition() {
        assertEquals(CueCategory.PHASE_TRANSITION, AudioCue.WodRoundComplete.category)
    }
}

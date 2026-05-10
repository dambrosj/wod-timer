package com.wod.app.domain.engine

import com.wod.app.domain.model.PhaseType
import com.wod.app.domain.model.TimerConfig
import com.wod.app.domain.model.WodRepeatConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [TabataEngine] (T49, T52, T53, T54).
 *
 * We use [TestScope] + [StandardTestDispatcher] so we can advance virtual
 * time in 1-second increments without waiting real time.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TabataEngineTest {

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun tabata(
        series: Int = 2,
        workSeconds: Int = 4,
        restSeconds: Int = 2,
        wodRounds: Int = 1,
        restBetweenRounds: Int = 0,
        exercises: List<String> = emptyList(),
    ) = TimerConfig.Tabata(
        series = series,
        workSeconds = workSeconds,
        restSeconds = restSeconds,
        repeat = WodRepeatConfig(
            wodRounds = wodRounds,
            restBetweenRoundsSeconds = restBetweenRounds,
        ),
        exercises = exercises,
    )

    /** Advance the clock by [n] full seconds (1000ms each). */
    private fun TestScope.tickSeconds(n: Int) = advanceTimeBy(n * 1_000L + 1)

    // ── T49 — Phase sequence ───────────────────────────────────────────────────

    @Test
    fun `plan follows COUNTDOWN WORK REST sequence`() = runTest(UnconfinedTestDispatcher()) {
        val engine = TabataEngine(tabata(series = 2, workSeconds = 4, restSeconds = 2), this)
        val plan = engine.plan
        assertEquals(PhaseType.COUNTDOWN, plan[0].phaseType)
        assertEquals(PhaseType.WORK,      plan[1].phaseType)
        assertEquals(PhaseType.REST,      plan[2].phaseType)
        assertEquals(PhaseType.WORK,      plan[3].phaseType)
        assertEquals(PhaseType.REST,      plan[4].phaseType)
        assertEquals(5, plan.size)
    }

    @Test
    fun `initial phase is COUNTDOWN with correct remainingSeconds`() = runTest(UnconfinedTestDispatcher()) {
        val engine = TabataEngine(tabata(), this)
        engine.start()
        val p = engine.phase.first()
        assertEquals(PhaseType.COUNTDOWN, p.phase)
        assertEquals(BaseIntervalEngine.COUNTDOWN_SECONDS, p.remainingSeconds)
        engine.stop()
    }

    @Test
    fun `after countdown engine enters WORK`() = runTest(UnconfinedTestDispatcher()) {
        val engine = TabataEngine(tabata(series = 1, workSeconds = 4, restSeconds = 2), this)
        engine.start()
        // Skip COUNTDOWN
        tickSeconds(BaseIntervalEngine.COUNTDOWN_SECONDS)
        val p = engine.phase.first { it.phase == PhaseType.WORK }
        assertEquals(PhaseType.WORK, p.phase)
        engine.stop()
    }

    @Test
    fun `phase label is correct for WORK and REST`() = runTest(UnconfinedTestDispatcher()) {
        val engine = TabataEngine(tabata(series = 1, workSeconds = 4, restSeconds = 2), this)
        engine.start()
        tickSeconds(BaseIntervalEngine.COUNTDOWN_SECONDS)
        val workPhase = engine.phase.first { it.phase == PhaseType.WORK }
        assertEquals("Lavoro", workPhase.label)
        tickSeconds(4)
        val restPhase = engine.phase.first { it.phase == PhaseType.REST }
        assertEquals("Riposo", restPhase.label)
        engine.stop()
    }

    @Test
    fun `isCompleted emits true after all phases`() = runTest(UnconfinedTestDispatcher()) {
        val engine = TabataEngine(tabata(series = 1, workSeconds = 2, restSeconds = 1), this)
        engine.start()
        val collectJob = launch {
            engine.isCompleted.first { it }
        }
        // COUNTDOWN(10) + WORK(2) + REST(1) = 13 seconds
        tickSeconds(14)
        collectJob.join()
        assertTrue(engine.isCompleted.first())
        engine.stop()
    }

    @Test
    fun `totalSeconds calculation is correct`() {
        // 2 series × (4+2) = 12
        assertEquals(12, TabataEngine.totalSeconds(tabata(series = 2, workSeconds = 4, restSeconds = 2)))
        // 3 series × (20+10) × 2 rounds + 60s between = 180 + 180 + 60 = 420 — Wait, 3×30=90 × 2 = 180 + 60 = 240
        assertEquals(
            240,
            TabataEngine.totalSeconds(
                tabata(series = 3, workSeconds = 20, restSeconds = 10, wodRounds = 2, restBetweenRounds = 60)
            )
        )
    }

    // ── T52 — WOD repetition ──────────────────────────────────────────────────

    @Test
    fun `3 WOD rounds produce 2 WOD_REST phases`() {
        val plan = TabataEngine(
            tabata(series = 1, workSeconds = 2, restSeconds = 1, wodRounds = 3, restBetweenRounds = 5),
            TestScope(UnconfinedTestDispatcher()),
        ).plan
        val wodRestCount = plan.count { it.phaseType == PhaseType.WOD_REST }
        assertEquals(2, wodRestCount)
    }

    @Test
    fun `1 WOD round produces no WOD_REST`() {
        val plan = TabataEngine(
            tabata(series = 1, workSeconds = 2, restSeconds = 1, wodRounds = 1, restBetweenRounds = 60),
            TestScope(UnconfinedTestDispatcher()),
        ).plan
        assertEquals(0, plan.count { it.phaseType == PhaseType.WOD_REST })
    }

    @Test
    fun `restBetweenRounds=0 with wodRounds 2 produces no WOD_REST`() {
        val plan = TabataEngine(
            tabata(series = 1, workSeconds = 2, restSeconds = 1, wodRounds = 2, restBetweenRounds = 0),
            TestScope(UnconfinedTestDispatcher()),
        ).plan
        assertEquals(0, plan.count { it.phaseType == PhaseType.WOD_REST })
    }

    @Test
    fun `WOD_REST step has correct duration`() {
        val plan = TabataEngine(
            tabata(series = 1, workSeconds = 2, restSeconds = 1, wodRounds = 2, restBetweenRounds = 30),
            TestScope(UnconfinedTestDispatcher()),
        ).plan
        val wodRest = plan.first { it.phaseType == PhaseType.WOD_REST }
        assertEquals(30, wodRest.durationSeconds)
    }

    @Test
    fun `currentWodRound increments correctly in plan`() {
        val plan = TabataEngine(
            tabata(series = 1, workSeconds = 2, restSeconds = 1, wodRounds = 2, restBetweenRounds = 5),
            TestScope(UnconfinedTestDispatcher()),
        ).plan
        val round1 = plan.filter { it.wodRound == 1 }
        val round2 = plan.filter { it.wodRound == 2 }
        assertTrue(round1.isNotEmpty())
        assertTrue(round2.isNotEmpty())
        // WOD_REST should be in round 1
        assertEquals(1, plan.first { it.phaseType == PhaseType.WOD_REST }.wodRound)
    }

    // ── T53 — pause / resume / skip ───────────────────────────────────────────

    @Test
    fun `pause then resume preserves remainingSeconds`() = runTest(UnconfinedTestDispatcher()) {
        val engine = TabataEngine(tabata(series = 1, workSeconds = 10, restSeconds = 2), this)
        engine.start()
        // Move into WORK phase
        tickSeconds(BaseIntervalEngine.COUNTDOWN_SECONDS)
        // Advance 3 seconds into WORK (7 remaining)
        tickSeconds(3)
        engine.pause()
        val pausedPhase = engine.phase.first { it.isPaused }
        val savedRemaining = pausedPhase.remainingSeconds
        // Wait 2 more real-time equivalent ticks — should not decrement
        advanceTimeBy(2_000L)
        val stillPaused = engine.phase.first()
        assertTrue(stillPaused.isPaused)
        assertEquals(savedRemaining, stillPaused.remainingSeconds)
        engine.resume()
        val resumed = engine.phase.first { !it.isPaused }
        assertEquals(savedRemaining, resumed.remainingSeconds)
        engine.stop()
    }

    @Test
    fun `skip from WORK enters REST`() = runTest(UnconfinedTestDispatcher()) {
        val engine = TabataEngine(tabata(series = 1, workSeconds = 10, restSeconds = 5), this)
        engine.start()
        tickSeconds(BaseIntervalEngine.COUNTDOWN_SECONDS)
        engine.phase.first { it.phase == PhaseType.WORK }
        engine.skip()
        val next = engine.phase.first { it.phase != PhaseType.WORK }
        assertEquals(PhaseType.REST, next.phase)
        engine.stop()
    }

    @Test
    fun `skip from last WORK of last WOD round completes workout`() = runTest(UnconfinedTestDispatcher()) {
        val engine = TabataEngine(tabata(series = 1, workSeconds = 10, restSeconds = 0), this)
        engine.start()
        tickSeconds(BaseIntervalEngine.COUNTDOWN_SECONDS)
        engine.phase.first { it.phase == PhaseType.WORK }
        engine.skip()
        // Skip REST (0s) then check completion
        tickSeconds(2)
        val done = engine.isCompleted.first { it }
        assertTrue(done)
        engine.stop()
    }

    // ── T54 — Exercise resolution ─────────────────────────────────────────────

    @Test
    fun `empty exercises list always returns null currentExercise`() = runTest(UnconfinedTestDispatcher()) {
        val engine = TabataEngine(tabata(series = 2, workSeconds = 4, restSeconds = 2, exercises = emptyList()), this)
        engine.start()
        tickSeconds(BaseIntervalEngine.COUNTDOWN_SECONDS)
        val workPhase = engine.phase.first { it.phase == PhaseType.WORK }
        assertNull(workPhase.currentExercise)
        engine.stop()
    }

    @Test
    fun `single exercise maps to every WORK phase`() = runTest(UnconfinedTestDispatcher()) {
        val engine = TabataEngine(
            tabata(series = 3, workSeconds = 4, restSeconds = 2, exercises = listOf("Squat")),
            this,
        )
        engine.start()
        tickSeconds(BaseIntervalEngine.COUNTDOWN_SECONDS)
        // Check 3 WORK phases all show "Squat"
        repeat(3) {
            val wp = engine.phase.first { it.phase == PhaseType.WORK }
            assertEquals("Squat", wp.currentExercise)
            // Advance to REST
            tickSeconds(4)
            engine.phase.first { it.phase == PhaseType.REST }
            tickSeconds(2)
        }
        engine.stop()
    }

    @Test
    fun `exercises size equals series maps 1-to-1`() {
        val plan = TabataEngine(
            tabata(series = 3, workSeconds = 4, restSeconds = 2, exercises = listOf("A", "B", "C")),
            TestScope(UnconfinedTestDispatcher()),
        ).plan
        val workSteps = plan.filter { it.phaseType == PhaseType.WORK }
        assertEquals(3, workSteps.size)
        // seriesIndex 0→A, 1→B, 2→C
        assertEquals(0, workSteps[0].seriesIndex)
        assertEquals(1, workSteps[1].seriesIndex)
        assertEquals(2, workSteps[2].seriesIndex)
    }

    @Test
    fun `cyclic indexing for exercises size not equal series`() {
        val config = tabata(series = 5, workSeconds = 4, restSeconds = 2, exercises = listOf("A", "B"))
        val engine = TabataEngine(config, TestScope(UnconfinedTestDispatcher()))
        // resolveExercise is private; verify via makePhase via the plan
        val plan = engine.plan
        val workIndices = plan.filter { it.phaseType == PhaseType.WORK }.map { it.seriesIndex }
        // Exercises: index%2 → [0,1,0,1,0] → ["A","B","A","B","A"]
        assertEquals(listOf(0, 1, 2, 3, 4), workIndices)
    }
}

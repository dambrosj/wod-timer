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
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [AmrapEngine] phase sequence and WOD repetitions (T49, T52).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AmrapEngineTest {

    private fun amrap(
        durationSeconds: Int = 10,
        wodRounds: Int = 1,
        restBetweenRounds: Int = 0,
    ) = TimerConfig.Amrap(
        durationSeconds = durationSeconds,
        repeat = WodRepeatConfig(
            wodRounds = wodRounds,
            restBetweenRoundsSeconds = restBetweenRounds,
        ),
    )

    private fun TestScope.tickSeconds(n: Int) = advanceTimeBy(n * 1_000L + 1)

    // ── T49 — Phase sequence ─────────────────────────────────────────────────

    @Test
    fun `plan for 1 round is COUNTDOWN then WORK`() {
        val plan = AmrapEngine(amrap(durationSeconds = 10, wodRounds = 1), TestScope(UnconfinedTestDispatcher())).plan
        assertEquals(2, plan.size)
        assertEquals(PhaseType.COUNTDOWN, plan[0].phaseType)
        assertEquals(PhaseType.WORK, plan[1].phaseType)
    }

    @Test
    fun `plan for 2 rounds with rest contains WOD_REST between rounds`() {
        val plan = AmrapEngine(amrap(durationSeconds = 10, wodRounds = 2, restBetweenRounds = 30), TestScope(UnconfinedTestDispatcher())).plan
        assertEquals(5, plan.size) // COUNTDOWN WORK WOD_REST COUNTDOWN WORK
        assertEquals(PhaseType.WOD_REST, plan[2].phaseType)
        assertEquals(30, plan[2].durationSeconds)
    }

    @Test
    fun `plan for 2 rounds with no rest omits WOD_REST`() {
        val plan = AmrapEngine(amrap(durationSeconds = 10, wodRounds = 2, restBetweenRounds = 0), TestScope(UnconfinedTestDispatcher())).plan
        assertEquals(4, plan.size) // COUNTDOWN WORK COUNTDOWN WORK
        assertTrue(plan.none { it.phaseType == PhaseType.WOD_REST })
    }

    @Test
    fun `initial phase is COUNTDOWN`() = runTest(UnconfinedTestDispatcher()) {
        val engine = AmrapEngine(amrap(durationSeconds = 10), this)
        engine.start()
        val p = engine.phase.first()
        assertEquals(PhaseType.COUNTDOWN, p.phase)
        assertEquals(BaseIntervalEngine.COUNTDOWN_SECONDS, p.remainingSeconds)
        engine.stop()
    }

    @Test
    fun `after countdown phase is WORK with label AMRAP`() = runTest(UnconfinedTestDispatcher()) {
        val engine = AmrapEngine(amrap(durationSeconds = 10), this)
        engine.start()
        tickSeconds(BaseIntervalEngine.COUNTDOWN_SECONDS)
        val p = engine.phase.first { it.phase == PhaseType.WORK }
        assertEquals("AMRAP", p.label)
        assertEquals(PhaseType.WORK, p.phase)
        engine.stop()
    }

    @Test
    fun `isCompleted emits true after single round completes`() = runTest(UnconfinedTestDispatcher()) {
        val engine = AmrapEngine(amrap(durationSeconds = 3), this)
        engine.start()
        val collectJob = launch { engine.isCompleted.first { it } }
        tickSeconds(BaseIntervalEngine.COUNTDOWN_SECONDS + 3 + 1)
        collectJob.join()
        assertTrue(engine.isCompleted.first())
        engine.stop()
    }

    @Test
    fun `totalSeconds calculation is correct`() {
        assertEquals(10, AmrapEngine.totalSeconds(amrap(durationSeconds = 10, wodRounds = 1)))
        // 2 rounds × 10 + 30 rest = 50
        assertEquals(50, AmrapEngine.totalSeconds(amrap(durationSeconds = 10, wodRounds = 2, restBetweenRounds = 30)))
    }

    // ── T52 — WOD repetition in AMRAP ───────────────────────────────────────

    @Test
    fun `3 rounds produce 2 WOD_REST steps`() {
        val plan = AmrapEngine(
            amrap(durationSeconds = 5, wodRounds = 3, restBetweenRounds = 10),
            TestScope(UnconfinedTestDispatcher()),
        ).plan
        assertEquals(2, plan.count { it.phaseType == PhaseType.WOD_REST })
    }

    @Test
    fun `wodRound=0 is treated as 1`() {
        val config = TimerConfig.Amrap(
            durationSeconds = 5,
            repeat = WodRepeatConfig(wodRounds = 0, restBetweenRoundsSeconds = 0),
        )
        val plan = AmrapEngine(config, TestScope(UnconfinedTestDispatcher())).plan
        // 0.coerceAtLeast(1) = 1 → COUNTDOWN + WORK = 2 steps
        assertEquals(2, plan.size)
    }

    // ── T53 — pause / resume / skip in AMRAP ────────────────────────────────

    @Test
    fun `pause halts countdown in AMRAP`() = runTest(UnconfinedTestDispatcher()) {
        val engine = AmrapEngine(amrap(durationSeconds = 20), this)
        engine.start()
        tickSeconds(2)
        engine.pause()
        val paused = engine.phase.first { it.isPaused }
        val savedRemaining = paused.remainingSeconds
        advanceTimeBy(5_000L)
        assertEquals(savedRemaining, engine.phase.first().remainingSeconds)
        engine.resume()
        val resumed = engine.phase.first { !it.isPaused }
        assertEquals(savedRemaining, resumed.remainingSeconds)
        engine.stop()
    }

    @Test
    fun `skip advances from COUNTDOWN to WORK in AMRAP`() = runTest(UnconfinedTestDispatcher()) {
        val engine = AmrapEngine(amrap(durationSeconds = 20), this)
        engine.start()
        engine.phase.first { it.phase == PhaseType.COUNTDOWN }
        engine.skip()
        val next = engine.phase.first { it.phase != PhaseType.COUNTDOWN }
        assertEquals(PhaseType.WORK, next.phase)
        engine.stop()
    }
}

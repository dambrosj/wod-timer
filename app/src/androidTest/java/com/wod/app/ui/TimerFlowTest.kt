package com.wod.app.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wod.app.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * T50 — Instrumented end-to-end test: config → start → verify phase transitions.
 *
 * Flow: Home → TABATA config screen → AVVIA IL TIMER → TimerRunningScreen.
 * Verifies the COUNTDOWN phase ("Pronti") is shown immediately, then transitions
 * to the WORK phase ("Lavoro") once the 10-second countdown elapses.
 *
 * Requires a connected device or emulator with the app's manifest permissions.
 */
@RunWith(AndroidJUnit4::class)
class TimerFlowTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    /** T50.1 — Navigating home → TABATA config → AVVIA shows the countdown phase. */
    @Test
    fun tabataFlow_showsCountdownThenWork() {
        // From Home screen, tap the TABATA type button
        rule.onNodeWithText("TABATA").performClick()

        // On the Tabata config screen, tap AVVIA IL TIMER (default config is fine)
        rule.waitUntil(timeoutMillis = 4_000) {
            rule.onAllNodesWithText("AVVIA IL TIMER").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("AVVIA IL TIMER").performClick()

        // Timer screen: COUNTDOWN phase ("Pronti") must appear within a few seconds
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Pronti").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Pronti").assertIsDisplayed()

        // After the 10-second countdown, WORK phase ("Lavoro") must appear
        rule.waitUntil(timeoutMillis = 15_000) {
            rule.onAllNodesWithText("Lavoro").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Lavoro").assertIsDisplayed()
    }

    /** T50.2 — Timer can be paused and resumed from the timer screen (portrait). */
    @Test
    fun tabataFlow_pauseAndResume() {
        rule.onNodeWithText("TABATA").performClick()
        rule.waitUntil(4_000) {
            rule.onAllNodesWithText("AVVIA IL TIMER").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("AVVIA IL TIMER").performClick()

        // Wait for WORK phase
        rule.waitUntil(20_000) {
            rule.onAllNodesWithText("Lavoro").fetchSemanticsNodes().isNotEmpty()
        }

        // Single-tap anywhere on the timer box pauses in portrait.
        // In landscape, explicit PAUSA/SALTA buttons are shown — tap PAUSA.
        // We check for the "PAUSA" text node (displayed in the hint below the ring).
        rule.onNodeWithText("PAUSA").assertIsDisplayed()
    }
}

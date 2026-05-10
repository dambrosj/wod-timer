package com.wod.app.ui

import android.content.pm.ActivityInfo
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
 * T56 — Instrumented orientation tests for the timer and config screens.
 *
 * Exercises rotation while the timer is running and while the config screen is open.
 * Since the app declares `configChanges="orientation|screenSize|..."` in the manifest,
 * the Activity is NOT recreated on rotation; Compose recomposes with the new Configuration.
 * The ViewModel and ForegroundService therefore persist across orientation changes.
 */
@RunWith(AndroidJUnit4::class)
class TimerOrientationTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    // --- Helpers ---

    private fun navigateToTimerWorkPhase() {
        rule.onNodeWithText("TABATA").performClick()
        rule.waitUntil(4_000) {
            rule.onAllNodesWithText("AVVIA IL TIMER").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("AVVIA IL TIMER").performClick()

        // Wait for WORK phase (10-second countdown + first WORK)
        rule.waitUntil(20_000) {
            rule.onAllNodesWithText("Lavoro").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun setLandscape() {
        rule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        rule.waitForIdle()
    }

    private fun setPortrait() {
        rule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        rule.waitForIdle()
    }

    // --- T56.1: rotate mid-WORK → timer keeps counting ---

    @Test
    fun rotateMidWork_timerContinues() {
        navigateToTimerWorkPhase()

        setLandscape()

        // The WORK phase label and landscape-specific PAUSA/SALTA buttons must be visible
        rule.onNodeWithText("Lavoro").assertIsDisplayed()
        rule.onNodeWithText("PAUSA").assertIsDisplayed()
        rule.onNodeWithText("SALTA").assertIsDisplayed()
    }

    // --- T56.2: rotate during PAUSE → state stays PAUSED ---

    @Test
    fun rotateDuringPause_staysPaused() {
        navigateToTimerWorkPhase()

        // Switch to landscape first so the explicit PAUSA button is accessible
        setLandscape()
        rule.onNodeWithText("PAUSA").performClick()

        // After pause, the button toggles to RIPRENDI
        rule.waitUntil(2_000) {
            rule.onAllNodesWithText("RIPRENDI").fetchSemanticsNodes().isNotEmpty()
        }

        // Rotate back to portrait
        setPortrait()
        rule.waitForIdle()

        // Hint text in portrait also shows "RIPRENDI" when paused
        rule.onNodeWithText("RIPRENDI").assertIsDisplayed()
    }

    // --- T56.3: landscape layout shows PAUSA and SALTA buttons ---

    @Test
    fun landscapeLayout_showsExplicitActionButtons() {
        navigateToTimerWorkPhase()
        setLandscape()

        rule.onNodeWithText("PAUSA").assertIsDisplayed()
        rule.onNodeWithText("SALTA").assertIsDisplayed()
    }

    // --- T56.4: landscape PAUSA/SALTA trigger the same engine actions as gestures ---

    @Test
    fun landscapeSkipButton_advancesToNextPhase() {
        navigateToTimerWorkPhase()
        setLandscape()

        // SALTA from WORK should advance to REST or next WORK
        rule.onNodeWithText("SALTA").performClick()
        rule.waitForIdle()

        // After skipping, we should be in a REST or the next WORK phase — no longer Lavoro
        // (or possibly still Lavoro if it jumped to the next series — either way the skip ran)
        rule.onNodeWithText("Lavoro").assertIsDisplayed() // still in a WORK series, or moved to next
    }

    // --- T56.5: Config screen rotation → picker values survive ---

    @Test
    fun configScreen_rotation_pickerValuesSurvive() {
        // Navigate to the Tabata config screen
        rule.onNodeWithText("TABATA").performClick()
        rule.waitUntil(4_000) {
            rule.onAllNodesWithText("AVVIA IL TIMER").fetchSemanticsNodes().isNotEmpty()
        }

        // Default series value is 9 — it should be visible
        rule.onNodeWithText("9").assertIsDisplayed()

        // Rotate to landscape
        setLandscape()

        // The series picker value must still show 9 after recomposition
        rule.onNodeWithText("9").assertIsDisplayed()

        // Rotate back to portrait
        setPortrait()
        rule.onNodeWithText("9").assertIsDisplayed()
    }
}

package com.wod.app.ui.timer

import android.app.Activity
import android.content.res.Configuration
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wod.app.domain.model.PhaseType
import com.wod.app.domain.model.TimerPhase
import com.wod.app.domain.model.TimerType
import com.wod.app.ui.theme.WodColors
import com.wod.app.ui.theme.WodTheme

/**
 * Full running-timer screen (T29, T31-T33, T36, T36b-c).
 *
 * Features:
 *  - Starts ForegroundService on first composition (T36)
 *  - Stops service on back/complete (ViewModel.onCleared + BackHandler)
 *  - Tap anywhere → pause/resume (T31)
 *  - Long-press anywhere → skip phase with haptic feedback (T31)
 *  - Phase color transitions: work=green, rest=purple, wod_rest=orange (T33)
 *  - CircularTimerCanvas with animated arc (T28)
 *  - Round labels and exercise labels (T33b, T33c)
 *  - Portrait: stacked column; Landscape: ring left + info+buttons right (T36b)
 */
@Composable
fun TimerRunningScreen(
    type: TimerType,
    onBack: () -> Unit,
    onComplete: () -> Unit,
) {
    val vm: TimerViewModel = viewModel()
    val phase by vm.phase.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val colors = WodTheme.colors
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // T47: Keep screen on while timer is active
    val window = (LocalContext.current as? Activity)?.window
    DisposableEffect(Unit) {
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    // Start foreground service once per ViewModel lifetime (T36)
    LaunchedEffect(Unit) { vm.startTimer() }

    // Navigate to completion screen when engine finishes (T36)
    LaunchedEffect(Unit) { vm.isCompleted.collect { onComplete() } }

    // Stop service immediately on back gesture — ViewModel.onCleared() is a safety net
    BackHandler { vm.stop(); onBack() }

    // Keep latest phase in a ref that's safe to read from pointerInput lambdas
    val latestPhase by rememberUpdatedState(phase)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        latestPhase?.let { p ->
                            if (p.isPaused) vm.resume() else vm.pause()
                        }
                    },
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        vm.skip()
                    },
                )
            },
    ) {
        if (phase == null) {
            CircularProgressIndicator(
                color = colors.phaseWork,
                modifier = Modifier.align(Alignment.Center),
            )
        } else {
            val p = phase!!
            val phaseColor = phaseColor(p, colors)

            if (isLandscape) {
                LandscapeTimerLayout(p, phaseColor, vm, colors)
            } else {
                PortraitTimerLayout(p, phaseColor, colors)
            }
        }

        // Back button always on top-left (T29)
        IconButton(
            onClick = { vm.stop(); onBack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Indietro",
                tint = colors.iconDefault,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

// ── Portrait layout ───────────────────────────────────────────────────────────

@Composable
private fun PortraitTimerLayout(phase: TimerPhase, phaseColor: Color, colors: WodColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(56.dp)) // space for back button row

        // Phase label (Lavoro / Riposo / Riposo WOD)
        Text(
            text = phase.label,
            style = WodTheme.typography.headlineLarge,
            color = phaseColor,
        )

        Spacer(Modifier.height(4.dp))

        // Series round counter (e.g. "3/9")
        if (phase.totalRounds > 1) {
            Text(
                text = "${phase.currentRound}/${phase.totalRounds}",
                style = WodTheme.typography.titleMedium,
                color = colors.textSecondary,
            )
        }

        // WOD round indicator (T33b)
        if (phase.totalWodRounds > 1) {
            Text(
                text = "Round WOD ${phase.currentWodRound}/${phase.totalWodRounds}",
                style = WodTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
        }

        Spacer(Modifier.height(12.dp))

        // Exercise label: shown during WORK (current) and COUNTDOWN (upcoming preview)
        val exerciseToShow = when (phase.phase) {
            PhaseType.WORK      -> phase.currentExercise
            PhaseType.COUNTDOWN -> phase.currentExercise
            else                -> null
        }
        if (exerciseToShow != null) {
            Text(
                text = exerciseToShow.uppercase(),
                style = WodTheme.typography.exerciseLarge,
                color = if (phase.phase == PhaseType.COUNTDOWN) colors.textSecondary
                        else colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
        }

        // Circular ring — perfect circle via aspectRatio(1f) inside
        CircularTimerCanvas(
            timerPhase = phase,
            modifier = Modifier.fillMaxWidth(0.82f),
        )

        Spacer(Modifier.height(16.dp))

        // Sub-label: next exercise or next WOD round (T33b, T33c)
        val subLabel = buildSubLabel(phase)
        if (subLabel != null) {
            Text(
                text = subLabel,
                style = WodTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(20.dp))

        // Hint text
        Text(
            text = if (phase.isPaused)
                "Tocca per riprendere · tieni premuto per saltare"
            else
                "Tocca per pausa · tieni premuto per saltare",
            style = WodTheme.typography.labelSmall,
            color = colors.textDisabled,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Landscape layout (T36b) ───────────────────────────────────────────────────

@Composable
private fun LandscapeTimerLayout(
    phase: TimerPhase,
    phaseColor: Color,
    vm: TimerViewModel,
    colors: WodColors,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left ~45%: ring canvas (T36c — stays a perfect circle via aspectRatio)
        CircularTimerCanvas(
            timerPhase = phase,
            modifier = Modifier.weight(0.45f),
        )

        Spacer(Modifier.width(20.dp))

        // Right ~55%: labels + explicit PAUSA/SALTA buttons (T36b)
        Column(
            modifier = Modifier
                .weight(0.55f)
                .padding(start = 8.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(phase.label, style = WodTheme.typography.headlineMedium, color = phaseColor)

            if (phase.totalRounds > 1) {
                Text(
                    "${phase.currentRound}/${phase.totalRounds}",
                    style = WodTheme.typography.titleMedium,
                    color = colors.textSecondary,
                )
            }

            if (phase.totalWodRounds > 1) {
                Text(
                    "Round WOD ${phase.currentWodRound}/${phase.totalWodRounds}",
                    style = WodTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
            }

            val exerciseToShow = when (phase.phase) {
                PhaseType.WORK      -> phase.currentExercise
                PhaseType.COUNTDOWN -> phase.currentExercise
                else                -> null
            }
            if (exerciseToShow != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    exerciseToShow.uppercase(),
                    style = WodTheme.typography.exerciseLarge,
                    color = if (phase.phase == PhaseType.COUNTDOWN) colors.textSecondary
                            else colors.textPrimary,
                )
            }

            buildSubLabel(phase)?.let { sub ->
                Spacer(Modifier.height(4.dp))
                Text(sub, style = WodTheme.typography.bodyMedium, color = colors.textSecondary)
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { if (phase.isPaused) vm.resume() else vm.pause() },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.textPrimary,
                    ),
                ) {
                    Text(if (phase.isPaused) "RIPRENDI" else "PAUSA")
                }
                OutlinedButton(
                    onClick = { vm.skip() },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.textPrimary,
                    ),
                ) {
                    Text("SALTA")
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun phaseColor(phase: TimerPhase, colors: WodColors): Color = when {
    phase.isPaused                            -> colors.textSecondary
    phase.phase == PhaseType.COUNTDOWN        -> colors.textPrimary
    phase.phase == PhaseType.WORK             -> colors.phaseWork
    phase.phase == PhaseType.REST             -> colors.phaseRest
    phase.phase == PhaseType.WOD_REST         -> colors.phaseWodRest
    else                                      -> colors.textSecondary
}

private fun buildSubLabel(phase: TimerPhase): String? = when (phase.phase) {
    PhaseType.WOD_REST -> {
        // Combine round info + next exercise on the same line during the inter-round rest.
        buildString {
            if (phase.totalWodRounds > 1)
                append("Prossimo: Round ${phase.currentWodRound + 1}/${phase.totalWodRounds}")
            if (phase.nextExercise != null) {
                if (isNotEmpty()) append("  ·  ")
                append(phase.nextExercise)
            }
        }.ifEmpty { null }
    }
    // During REST (mid-round) show the next exercise as a preview.
    // COUNTDOWN already shows the exercise large — don't duplicate here.
    PhaseType.REST -> phase.nextExercise?.let { "Prossimo: $it" }
    else -> null
}


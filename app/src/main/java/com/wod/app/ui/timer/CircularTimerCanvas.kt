package com.wod.app.ui.timer

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.sp
import com.wod.app.domain.model.PhaseType
import com.wod.app.domain.model.TimerPhase
import com.wod.app.ui.theme.WodTheme
import kotlin.math.PI
import kotlin.math.min

/**
 * Circular timer ring composable (T28).
 *
 * Visual elements:
 *  - 60-segment dashed ring in [WodColors.ringDashes]
 *  - Colored arc depleting clockwise from 12 o'clock;
 *    color transitions smoothly between phases
 *  - Center: countdown text (MM:SS or single number)
 *
 * Always maintains a perfect 1:1 aspect ratio via [Modifier.aspectRatio].
 * Use [Modifier.fillMaxWidth] or a fixed size on the caller side.
 */
@Composable
fun CircularTimerCanvas(
    timerPhase: TimerPhase,
    modifier: Modifier = Modifier,
) {
    val colors = WodTheme.colors
    val typography = WodTheme.typography

    val targetArcColor = when {
        timerPhase.isPaused                            -> colors.textSecondary
        timerPhase.phase == PhaseType.COUNTDOWN        -> colors.textPrimary
        timerPhase.phase == PhaseType.WORK             -> colors.phaseWork
        timerPhase.phase == PhaseType.REST             -> colors.phaseRest
        timerPhase.phase == PhaseType.WOD_REST         -> colors.phaseWodRest
        else                                           -> colors.textSecondary
    }
    val arcColor by animateColorAsState(
        targetValue = targetArcColor,
        animationSpec = tween(durationMillis = 500),
        label = "arcColor",
    )

    val targetProgress = if (timerPhase.totalSeconds > 0)
        timerPhase.remainingSeconds.toFloat() / timerPhase.totalSeconds.toFloat()
    else 1f
    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 900),
        label = "timerProgress",
    )

    // COUNTDOWN phase: always show large single number; otherwise MM:SS for ≥60s
    val isCountdown = timerPhase.phase == PhaseType.COUNTDOWN
    val timerText = when {
        isCountdown -> "${timerPhase.remainingSeconds}"
        timerPhase.remainingSeconds >= 60 ->
            "%d:%02d".format(timerPhase.remainingSeconds / 60, timerPhase.remainingSeconds % 60)
        else -> "${timerPhase.remainingSeconds}"
    }
    // Scale font to fit inside the ring: 1-2 chars → 96sp, 3-4 chars (M:SS) → 72sp, 5+ (MM:SS) → 52sp
    val timerStyle = when {
        isCountdown        -> typography.displayCountdown
        timerText.length <= 2 -> typography.displayTimer                          // e.g. "45"
        timerText.length <= 4 -> typography.displayTimer.copy(fontSize = 72.sp)  // e.g. "9:45"
        else               -> typography.displayTimer.copy(fontSize = 52.sp)     // e.g. "10:00"
    }

    Box(modifier = modifier.aspectRatio(1f)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = min(size.width, size.height) / 2f * 0.82f
            val center = Offset(size.width / 2f, size.height / 2f)
            val strokeWidth = min(size.width, size.height) * 0.055f

            // Dashed static background ring — 60 evenly-spaced marks
            val dashCount = 60
            val circumference = 2f * PI.toFloat() * radius
            val dashLen = circumference / dashCount * 0.35f
            val gapLen  = circumference / dashCount * 0.65f
            drawCircle(
                color = colors.ringDashes,
                radius = radius,
                center = center,
                style = Stroke(
                    width = strokeWidth,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLen, gapLen), 0f),
                ),
            )

            // Colored arc: starts at 12 o'clock (−90°), sweeps clockwise by progress fraction
            val sweepAngle = progress * 360f
            if (sweepAngle > 0.5f) {
                drawArc(
                    color = arcColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
        }

        Text(
            text = timerText,
            style = timerStyle,
            color = if (timerPhase.isPaused) colors.textSecondary else colors.textPrimary,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

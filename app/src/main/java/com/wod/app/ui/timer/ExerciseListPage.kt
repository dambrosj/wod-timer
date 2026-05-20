package com.wod.app.ui.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wod.app.domain.model.TimerPhase
import com.wod.app.ui.theme.WodTheme

/**
 * Page 2 of the AMRAP/FOR TIME timer — reachable by swiping left from the ring page.
 *
 * Layout (portrait & landscape — single scrollable column, adapts naturally):
 *
 * ┌────────────────────────────────────────────────────┐
 * │ MiniTimerBar: LAVORO  •  4:32  ████████░░░░░░      │
 * ├────────────────────────────────────────────────────┤
 * │ [✓] PUSHUP     Obiettivo: 20    [−][8][+] fatte    │
 * │     Min: 5                                         │
 * │ ────────────────────────────────────────────────── │
 * │ [ ] SQUAT      Obiettivo: 15    [−][0][+] fatte    │
 * └────────────────────────────────────────────────────┘
 *
 * @param exercises       Exercise names from config.
 * @param exerciseReps    Target reps per exercise (0 = not set).
 * @param exerciseMinReps Min reps before pause is allowed (0 = no constraint).
 * @param repsDone        Mutable state — reps done so far per exercise (lifted from parent).
 * @param checked         Mutable state — exercise marked as completed (lifted from parent).
 * @param phase           Current timer phase snapshot.
 * @param phaseColor      Color matching the current phase (work / rest / …).
 * @param onRepsDoneChange Called with the updated full list when a rep counter changes.
 * @param onCheckedChange  Called with the updated full list when a checkbox changes.
 */
@Composable
fun ExerciseListPage(
    exercises: List<String>,
    exerciseReps: List<Int>,
    exerciseMinReps: List<Int>,
    repsDone: List<Int>,
    checked: List<Boolean>,
    phase: TimerPhase,
    phaseColor: Color,
    onRepsDoneChange: (List<Int>) -> Unit,
    onCheckedChange: (List<Boolean>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WodTheme.colors
    val typography = WodTheme.typography

    // Helpers to update a specific index in a parallel list.
    fun List<Int>.withUpdated(i: Int, v: Int): List<Int> {
        val m = this.toMutableList()
        while (m.size <= i) m.add(0)
        m[i] = v
        return m
    }

    fun List<Boolean>.withUpdated(i: Int, v: Boolean): List<Boolean> {
        val m = this.toMutableList()
        while (m.size <= i) m.add(false)
        m[i] = v
        return m
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgPrimary),
    ) {
        // ── Mini timer bar ─────────────────────────────────────────────────────
        MiniTimerBar(phase = phase, phaseColor = phaseColor)

        HorizontalDivider(color = colors.divider)

        // ── Exercise list ──────────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = WodTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            itemsIndexed(exercises) { i, exercise ->
                val isDone = checked.getOrElse(i) { false }
                val done = repsDone.getOrElse(i) { 0 }
                val target = exerciseReps.getOrElse(i) { 0 }
                val minR = exerciseMinReps.getOrElse(i) { 0 }

                ExerciseRow(
                    name = exercise.ifBlank { "Esercizio ${i + 1}" },
                    targetReps = target,
                    minReps = minR,
                    repsDone = done,
                    isChecked = isDone,
                    onCheckedChange = { onCheckedChange(checked.withUpdated(i, it)) },
                    onRepsDoneChange = { onRepsDoneChange(repsDone.withUpdated(i, it)) },
                )

                if (i < exercises.lastIndex) {
                    HorizontalDivider(
                        color = colors.divider,
                        modifier = Modifier.padding(start = 48.dp),
                    )
                }
            }

            item { Spacer(Modifier.height(WodTheme.spacing.l)) }
        }
    }
}

// ── Mini timer bar ─────────────────────────────────────────────────────────────

@Composable
private fun MiniTimerBar(phase: TimerPhase, phaseColor: Color) {
    val colors = WodTheme.colors
    val typography = WodTheme.typography

    val progress = if (phase.totalSeconds > 0)
        (phase.totalSeconds - phase.remainingSeconds).toFloat() / phase.totalSeconds.toFloat()
    else 0f

    val timeText = phase.remainingSeconds.let { s ->
        if (s >= 60) "%d:%02d".format(s / 60, s % 60) else "$s"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = WodTheme.spacing.m, vertical = WodTheme.spacing.s),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WodTheme.spacing.s),
        ) {
            // Phase dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (phase.isPaused) colors.textDisabled else phaseColor),
            )

            // Phase label
            Text(
                text = if (phase.isPaused) "IN PAUSA" else phase.label.uppercase(),
                style = typography.labelSmall,
                color = if (phase.isPaused) colors.textDisabled else phaseColor,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.weight(1f))

            // Time remaining
            Text(
                text = timeText,
                style = typography.titleMedium,
                color = colors.textPrimary,
            )
        }

        // Thin progress bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = if (phase.isPaused) colors.textDisabled else phaseColor,
            trackColor = colors.divider,
        )
    }
}

// ── Exercise row ───────────────────────────────────────────────────────────────

@Composable
private fun ExerciseRow(
    name: String,
    targetReps: Int,
    minReps: Int,
    repsDone: Int,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onRepsDoneChange: (Int) -> Unit,
) {
    val colors = WodTheme.colors
    val typography = WodTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = WodTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Checkbox
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = colors.phaseWork,
                uncheckedColor = colors.textSecondary,
                checkmarkColor = colors.bgPrimary,
            ),
            modifier = Modifier.size(40.dp),
        )

        // Name + min-reps subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = typography.bodyMedium,
                color = if (isChecked) colors.textDisabled else colors.textPrimary,
                textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (minReps > 0 && !isChecked) {
                Text(
                    text = "Min: $minReps rep",
                    style = typography.labelSmall,
                    color = colors.textSecondary,
                )
            }
        }

        // Target reps badge (only if set)
        if (targetReps > 0) {
            Text(
                text = "× $targetReps",
                style = typography.bodyMedium,
                color = if (isChecked) colors.textDisabled else colors.textSecondary,
                modifier = Modifier.padding(horizontal = WodTheme.spacing.s),
            )
        }

        // Reps-done stepper
        DoneRepsStepper(
            value = repsDone,
            onValueChange = onRepsDoneChange,
            enabled = !isChecked,
        )
    }
}

// ── Compact done-reps stepper ──────────────────────────────────────────────────

@Composable
private fun DoneRepsStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val colors = WodTheme.colors
    val typography = WodTheme.typography

    Row(
        modifier = modifier
            .width(88.dp)
            .border(1.dp, if (enabled) colors.divider else colors.divider.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .background(colors.bgElevated, RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(
            onClick = { if (enabled && value > 0) onValueChange(value - 1) },
            modifier = Modifier.size(28.dp),
            enabled = enabled && value > 0,
        ) {
            Text(
                "−",
                style = typography.titleMedium,
                color = if (enabled && value > 0) colors.textSecondary else colors.textDisabled,
            )
        }
        Box(contentAlignment = Alignment.Center, modifier = Modifier.width(32.dp)) {
            Text(
                text = value.toString(),
                style = typography.bodyMedium,
                color = if (enabled) colors.textPrimary else colors.textDisabled,
            )
        }
        IconButton(
            onClick = { if (enabled) onValueChange(value + 1) },
            modifier = Modifier.size(28.dp),
            enabled = enabled,
        ) {
            Text(
                "+",
                style = typography.titleMedium,
                color = if (enabled) colors.textSecondary else colors.textDisabled,
            )
        }
    }
}

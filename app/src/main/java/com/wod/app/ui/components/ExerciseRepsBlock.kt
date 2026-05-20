package com.wod.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wod.app.ui.theme.WodTheme

/**
 * Collapsible block for setting target reps and minimum reps per exercise —
 * shown in AMRAP and FOR TIME config screens after the [ExercisesBlock].
 *
 * Only rendered when [exercises] is non-empty.
 *
 * Each row: [exercise name] | [reps badge] | [min-reps badge]
 * Tapping a badge opens a drum-roll bottom sheet (0–99, 0 = not set → "—").
 *
 * @param exercises       Exercise names (read-only, comes from ExercisesBlock state).
 * @param reps            Target reps per exercise.
 * @param minReps         Minimum reps before pause is allowed (0 = no constraint).
 * @param onRepsChange    Called with the updated full reps list.
 * @param onMinRepsChange Called with the updated full min-reps list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseRepsBlock(
    exercises: List<String>,
    reps: List<Int>,
    minReps: List<Int>,
    onRepsChange: (List<Int>) -> Unit,
    onMinRepsChange: (List<Int>) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (exercises.isEmpty()) return

    val colors = WodTheme.colors
    val typography = WodTheme.typography
    var expanded by rememberSaveable { mutableStateOf(false) }

    // Wheel items: index 0 → "—" (= value 0), index N → "$N"
    val pickerItems = remember { (0..99).map { if (it == 0) "—" else it.toString() } }

    // Which badge is open: Triple(exerciseIndex, isReps, currentValue)
    var openPicker by remember { mutableStateOf<Triple<Int, Boolean, Int>?>(null) }
    var tempPickerValue by remember { mutableIntStateOf(0) }

    // Helpers to safely update a specific index in a parallel list.
    fun List<Int>.withUpdatedAt(i: Int, v: Int): List<Int> {
        val base = this.toMutableList()
        while (base.size <= i) base.add(0)
        base[i] = v
        return base
    }

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(color = colors.divider)

        // ── Section header ────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = WodTheme.spacing.m, horizontal = WodTheme.spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ripetizioni per esercizio",
                    style = typography.bodyLarge,
                    color = colors.textPrimary,
                )
                Text(
                    text = "Obiettivo e ripetizioni minime prima della pausa",
                    style = typography.labelSmall,
                    color = colors.textSecondary,
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = colors.textSecondary,
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bgSurface)
                    .padding(WodTheme.spacing.m),
                verticalArrangement = Arrangement.spacedBy(WodTheme.spacing.s),
            ) {
                // Header row
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Obiettivo",
                        style = typography.labelSmall,
                        color = colors.textSecondary,
                        modifier = Modifier.width(72.dp),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Minimo",
                        style = typography.labelSmall,
                        color = colors.textSecondary,
                        modifier = Modifier.width(72.dp),
                        textAlign = TextAlign.Center,
                    )
                }

                HorizontalDivider(color = colors.divider)

                exercises.forEachIndexed { i, exercise ->
                    val repVal = reps.getOrElse(i) { 0 }
                    val minVal = minReps.getOrElse(i) { 0 }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = exercise.ifBlank { "Esercizio ${i + 1}" },
                            style = typography.bodyMedium,
                            color = colors.textPrimary,
                            modifier = Modifier.weight(1f),
                        )

                        // Obiettivo badge
                        RepsBadge(
                            value = repVal,
                            modifier = Modifier.width(72.dp),
                            onClick = {
                                tempPickerValue = repVal
                                openPicker = Triple(i, true, repVal)
                            },
                        )

                        Spacer(Modifier.width(8.dp))

                        // Minimo badge
                        RepsBadge(
                            value = minVal,
                            modifier = Modifier.width(72.dp),
                            onClick = {
                                tempPickerValue = minVal
                                openPicker = Triple(i, false, minVal)
                            },
                        )
                    }

                    if (i < exercises.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = WodTheme.spacing.xs),
                            color = colors.divider,
                        )
                    }
                }

                Spacer(Modifier.height(WodTheme.spacing.xs))
            }
        }

        HorizontalDivider(color = colors.divider)
    }

    // ── Drum-roll bottom sheet ─────────────────────────────────────────────────
    openPicker?.let { (exIdx, isReps, _) ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val label = if (isReps) "Ripetizioni obiettivo" else "Ripetizioni minime"

        ModalBottomSheet(
            onDismissRequest = { openPicker = null },
            sheetState = sheetState,
            containerColor = colors.bgPrimary,
            dragHandle = { BottomSheetDefaults.DragHandle(color = colors.divider) },
        ) {
            Text(
                text = label,
                style = typography.titleMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            )

            WheelPicker(
                items = pickerItems,
                selectedIndex = tempPickerValue.coerceIn(0, pickerItems.lastIndex),
                onIndexChanged = { tempPickerValue = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isReps) {
                        onRepsChange(reps.withUpdatedAt(exIdx, tempPickerValue))
                    } else {
                        onMinRepsChange(minReps.withUpdatedAt(exIdx, tempPickerValue))
                    }
                    openPicker = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accentTabata,
                    contentColor = Color.White,
                ),
            ) {
                Text("Conferma", style = typography.titleMedium)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Tappable reps badge ────────────────────────────────────────────────────────

@Composable
private fun RepsBadge(
    value: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WodTheme.colors
    val typography = WodTheme.typography

    val hasValue = value > 0

    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .height(44.dp)
            .border(
                width = if (hasValue) 2.dp else 1.dp,
                color = if (hasValue) colors.accentTabata else colors.divider,
                shape = RoundedCornerShape(12.dp),
            )
            .background(colors.bgSurface, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (hasValue) value.toString() else "—",
            style = typography.titleMedium,
            color = if (hasValue) colors.textPrimary else colors.textDisabled,
            textAlign = TextAlign.Center,
        )
    }
}

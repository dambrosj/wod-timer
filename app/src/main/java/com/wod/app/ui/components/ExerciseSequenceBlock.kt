package com.wod.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wod.app.ui.theme.WodTheme

/**
 * Collapsible block for building a free-form exercise sequence in AMRAP and
 * FOR TIME config screens.
 *
 * Each row: [#] [exercise name] [reps badge] [min-reps badge] [delete]
 * Tapping a badge opens a drum-roll bottom sheet (0 = "—" = not set, 1–99).
 *
 * The three parallel lists ([exercises], [exerciseReps], [exerciseMinReps]) are
 * always kept the same length and updated atomically.
 *
 * @param exercises       Exercise names.
 * @param exerciseReps    Target reps per exercise (0 = not set).
 * @param exerciseMinReps Min reps before pause is allowed (0 = no constraint).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSequenceBlock(
    exercises: List<String>,
    exerciseReps: List<Int>,
    exerciseMinReps: List<Int>,
    onExercisesChange: (List<String>) -> Unit,
    onRepsChange: (List<Int>) -> Unit,
    onMinRepsChange: (List<Int>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WodTheme.colors
    val typography = WodTheme.typography

    var expanded by rememberSaveable { mutableStateOf(exercises.isNotEmpty()) }

    // Wheel items: index 0 → "—" (value 0), index N → "$N"
    val pickerItems = remember { (0..99).map { if (it == 0) "—" else it.toString() } }

    // Which badge opened the picker: Triple(exerciseIndex, isReps, currentValue)
    var openPicker by remember { mutableStateOf<Triple<Int, Boolean, Int>?>(null) }
    var tempPickerValue by remember { mutableIntStateOf(0) }

    // ── Helpers ───────────────────────────────────────────────────────────────

    fun <T> List<T>.withUpdatedAt(i: Int, v: T, default: T): List<T> {
        val m = this.toMutableList()
        while (m.size <= i) m.add(default)
        m[i] = v
        return m
    }

    fun List<String>.withRemovedAt(i: Int): List<String> =
        filterIndexed { idx, _ -> idx != i }

    fun List<Int>.withRemovedAt(i: Int): List<Int> =
        filterIndexed { idx, _ -> idx != i }

    // Ensure all three lists have the same length as exercises before calling back.
    fun normalise(
        exList: List<String>,
        repList: List<Int>,
        minList: List<Int>,
    ): Triple<List<String>, List<Int>, List<Int>> {
        val n = exList.size
        return Triple(
            exList,
            List(n) { i -> repList.getOrElse(i) { 0 } },
            List(n) { i -> minList.getOrElse(i) { 0 } },
        )
    }

    fun addExercise() {
        val (ex, rp, mn) = normalise(exercises + "", exerciseReps, exerciseMinReps)
        onExercisesChange(ex); onRepsChange(rp); onMinRepsChange(mn)
    }

    fun removeExercise(i: Int) {
        val (ex, rp, mn) = normalise(
            exercises.withRemovedAt(i),
            exerciseReps.withRemovedAt(i),
            exerciseMinReps.withRemovedAt(i),
        )
        onExercisesChange(ex); onRepsChange(rp); onMinRepsChange(mn)
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(color = colors.divider)

        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = WodTheme.spacing.m, horizontal = WodTheme.spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sequenza esercizi",
                    style = typography.bodyLarge,
                    color = colors.textPrimary,
                )
                val subtitle = when (exercises.size) {
                    0    -> "Nessun esercizio"
                    1    -> "1 esercizio"
                    else -> "${exercises.size} esercizi"
                }
                Text(
                    text = subtitle,
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
                    .padding(horizontal = WodTheme.spacing.m),
            ) {
                // Column headers (only when there is at least one exercise)
                if (exercises.isNotEmpty()) {
                    Spacer(Modifier.height(WodTheme.spacing.s))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 28.dp, end = 36.dp),
                    ) {
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "Reps",
                            style = typography.labelSmall,
                            color = colors.textSecondary,
                            modifier = Modifier.width(52.dp),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Min",
                            style = typography.labelSmall,
                            color = colors.textSecondary,
                            modifier = Modifier.width(52.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                exercises.forEachIndexed { i, exercise ->
                    Spacer(Modifier.height(WodTheme.spacing.s))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        // Row number
                        Text(
                            text = "${i + 1}.",
                            style = typography.labelSmall,
                            color = colors.textDisabled,
                            modifier = Modifier.width(22.dp),
                            textAlign = TextAlign.End,
                        )

                        // Exercise name field
                        OutlinedTextField(
                            value = exercise,
                            onValueChange = { v ->
                                onExercisesChange(exercises.withUpdatedAt(i, v, ""))
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = {
                                Text(
                                    "Esercizio ${i + 1}",
                                    style = typography.labelSmall,
                                    color = colors.textDisabled,
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = if (i < exercises.lastIndex) ImeAction.Next else ImeAction.Done,
                            ),
                            textStyle = typography.bodyMedium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                focusedBorderColor = colors.accentTabata,
                                unfocusedBorderColor = colors.divider,
                                focusedLabelColor = colors.accentTabata,
                                unfocusedLabelColor = colors.textSecondary,
                                cursorColor = colors.accentTabata,
                            ),
                        )

                        // Target reps badge
                        val repVal = exerciseReps.getOrElse(i) { 0 }
                        SmallRepsBadge(
                            value = repVal,
                            modifier = Modifier.width(52.dp),
                            onClick = {
                                tempPickerValue = repVal
                                openPicker = Triple(i, true, repVal)
                            },
                        )

                        // Min reps badge
                        val minVal = exerciseMinReps.getOrElse(i) { 0 }
                        SmallRepsBadge(
                            value = minVal,
                            modifier = Modifier.width(52.dp),
                            onClick = {
                                tempPickerValue = minVal
                                openPicker = Triple(i, false, minVal)
                            },
                        )

                        // Delete button
                        IconButton(
                            onClick = { removeExercise(i) },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Rimuovi",
                                tint = colors.textDisabled,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(WodTheme.spacing.m))

                // Add exercise button
                TextButton(
                    onClick = { addExercise() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = colors.accentTabata,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Aggiungi esercizio",
                        style = typography.bodyMedium,
                        color = colors.accentTabata,
                    )
                }

                Spacer(Modifier.height(WodTheme.spacing.s))
            }
        }

        HorizontalDivider(color = colors.divider)
    }

    // ── Drum-roll bottom sheet ─────────────────────────────────────────────────
    openPicker?.let { (exIdx, isReps, _) ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val label = if (isReps) "Ripetizioni obiettivo" else "Ripetizioni minime"
        val exName = exercises.getOrElse(exIdx) { "" }
            .ifBlank { "Esercizio ${exIdx + 1}" }

        ModalBottomSheet(
            onDismissRequest = { openPicker = null },
            sheetState = sheetState,
            containerColor = colors.bgPrimary,
            dragHandle = { BottomSheetDefaults.DragHandle(color = colors.divider) },
        ) {
            Text(
                text = "$exName — $label",
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
                        onRepsChange(exerciseReps.withUpdatedAt(exIdx, tempPickerValue, 0))
                    } else {
                        onMinRepsChange(exerciseMinReps.withUpdatedAt(exIdx, tempPickerValue, 0))
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

// ── Compact badge ──────────────────────────────────────────────────────────────

@Composable
private fun SmallRepsBadge(
    value: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WodTheme.colors
    val typography = WodTheme.typography
    val hasValue = value > 0

    Box(
        modifier = modifier
            .height(52.dp)  // match TextField height
            .border(
                width = if (hasValue) 2.dp else 1.dp,
                color = if (hasValue) colors.accentTabata else colors.divider,
                shape = RoundedCornerShape(4.dp),
            )
            .background(colors.bgSurface, RoundedCornerShape(4.dp))
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

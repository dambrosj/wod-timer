package com.wod.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.wod.app.ui.theme.WodTheme

/**
 * Collapsible "Esercizi" block for all config screens (T21c).
 *
 * Two editing modes:
 *  • **Stesso per tutte** — a single field applies to every series.
 *  • **Per serie** — one field per series; the engine resolves them 1:1 (or
 *    cyclically if the list is shorter than the series count — PRD §4).
 *
 * Trailing blank fields are stripped before calling [onExercisesChange] so
 * the stored list stays clean.  Middle blanks are preserved: a blank in the
 * middle means "no label for that series" (engine returns null, timer shows
 * nothing).
 *
 * @param exercises      Current list coming from the parent state.
 * @param seriesCount    Total series; drives how many fields appear in per-serie mode.
 * @param onExercisesChange  Called whenever the list changes.
 */
@Composable
fun ExercisesBlock(
    exercises: List<String>,
    seriesCount: Int,
    onExercisesChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WodTheme.colors

    // ── Collapse state ────────────────────────────────────────────────────────
    var expanded by rememberSaveable { mutableStateOf(exercises.isNotEmpty()) }

    // ── Toggle: stesso / per serie ────────────────────────────────────────────
    // Derive initial mode from the incoming exercises list.
    var isPerSeries by rememberSaveable {
        mutableStateOf(exercises.size != 1) // size 1 → stesso; 0 or >1 → per serie
    }

    // ── Single-value field (stesso per tutte) ─────────────────────────────────
    var singleValue by rememberSaveable {
        mutableStateOf(if (exercises.size == 1) exercises[0] else "")
    }

    // ── Per-series values ─────────────────────────────────────────────────────
    // Initialised from exercises; extended/trimmed when seriesCount changes.
    val perSeriesValues: SnapshotStateList<String> = remember {
        List(seriesCount) { i -> exercises.getOrElse(i) { "" } }.toMutableStateList()
    }

    // Resize the list when the user changes the series picker.
    LaunchedEffect(seriesCount) {
        while (perSeriesValues.size < seriesCount) perSeriesValues.add("")
        while (perSeriesValues.size > seriesCount) perSeriesValues.removeLastOrNull()
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
            Text(
                text = "Esercizi",
                style = WodTheme.typography.bodyLarge,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
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
                verticalArrangement = Arrangement.spacedBy(WodTheme.spacing.m),
            ) {
                // ── Mode toggle ───────────────────────────────────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(WodTheme.spacing.s)) {
                    FilterChip(
                        selected = !isPerSeries,
                        onClick = {
                            isPerSeries = false
                            onExercisesChange(
                                if (singleValue.isBlank()) emptyList() else listOf(singleValue)
                            )
                        },
                        label = { Text("Stesso per tutte", style = WodTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.accentTabata,
                            selectedLabelColor = Color.White,
                            labelColor = colors.textSecondary,
                        ),
                    )
                    FilterChip(
                        selected = isPerSeries,
                        onClick = {
                            isPerSeries = true
                            // Prefill all fields with singleValue if they are all blank
                            if (singleValue.isNotBlank() && perSeriesValues.all { it.isBlank() }) {
                                for (i in perSeriesValues.indices) perSeriesValues[i] = singleValue
                            }
                            onExercisesChange(perSeriesValues.toList().dropLastWhile { it.isBlank() })
                        },
                        label = { Text("Per serie", style = WodTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.accentTabata,
                            selectedLabelColor = Color.White,
                            labelColor = colors.textSecondary,
                        ),
                    )
                }

                // ── Fields ────────────────────────────────────────────────────
                if (!isPerSeries) {
                    ExerciseField(
                        value = singleValue,
                        label = "Esercizio (tutte le serie)",
                        imeAction = ImeAction.Done,
                        onValueChange = { v ->
                            singleValue = v
                            onExercisesChange(if (v.isBlank()) emptyList() else listOf(v))
                        },
                    )
                } else {
                    perSeriesValues.forEachIndexed { index, value ->
                        ExerciseField(
                            value = value,
                            label = "Serie ${index + 1}",
                            imeAction = if (index < perSeriesValues.lastIndex) ImeAction.Next else ImeAction.Done,
                            onValueChange = { v ->
                                perSeriesValues[index] = v
                                onExercisesChange(
                                    perSeriesValues.toList().dropLastWhile { it.isBlank() }
                                )
                            },
                        )
                    }
                }

                Spacer(Modifier.height(WodTheme.spacing.xs))
            }
        }

        HorizontalDivider(color = colors.divider)
    }
}

// ── Private helpers ────────────────────────────────────────────────────────────

@Composable
private fun ExerciseField(
    value: String,
    label: String,
    imeAction: ImeAction,
    onValueChange: (String) -> Unit,
) {
    val colors = WodTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = WodTheme.typography.labelSmall) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = imeAction,
        ),
        textStyle = WodTheme.typography.bodyMedium,
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
}

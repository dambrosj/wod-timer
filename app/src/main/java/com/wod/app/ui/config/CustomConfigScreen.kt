package com.wod.app.ui.config

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
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wod.app.WodApp
import com.wod.app.domain.engine.CustomEngine
import com.wod.app.domain.model.CustomInterval
import com.wod.app.domain.model.TimerConfig
import com.wod.app.domain.model.WodRepeatConfig
import com.wod.app.ui.components.WheelPicker
import com.wod.app.ui.components.WodRepeatBlock
import com.wod.app.ui.theme.WodPreview
import com.wod.app.ui.theme.WodTheme
import kotlinx.coroutines.launch

/**
 * CUSTOM timer configuration screen.
 *
 * The user builds a free-form ordered list of named intervals (name + duration each),
 * then optionally enables WOD repetitions via [WodRepeatBlock].
 *
 * Each interval row:  [index]  [name field]  [MM:SS badge]  [delete]
 */
@Composable
fun CustomConfigScreen(
    onBack: () -> Unit,
    onStart: (TimerConfig.Custom) -> Unit,
    initialConfig: TimerConfig.Custom? = null,
    onSaveEdited: ((TimerConfig.Custom) -> Unit)? = null,
    initialWodName: String? = null,
    initialWodDesc: String? = null,
    onRenameWod: ((String, String) -> Unit)? = null,
) {
    val defaultIntervals = listOf(CustomInterval("", 30))
    var intervals by rememberSaveable(stateSaver = CustomIntervalListSaver) {
        mutableStateOf(initialConfig?.intervals?.takeIf { it.isNotEmpty() } ?: defaultIntervals)
    }
    var repeat by rememberSaveable(stateSaver = WodRepeatConfigSaver) {
        mutableStateOf(initialConfig?.repeat ?: WodRepeatConfig())
    }
    var notes by rememberSaveable { mutableStateOf(initialConfig?.notes ?: "") }

    val config = TimerConfig.Custom(intervals = intervals, repeat = repeat, notes = notes)

    val scope = rememberCoroutineScope()
    val wodApp = LocalContext.current.applicationContext as WodApp
    val colors = WodTheme.colors

    ConfigScaffold(
        title = "CUSTOM",
        totalSeconds = CustomEngine.totalSeconds(config),
        onBack = onBack,
        onStart = { if (onSaveEdited != null) onSaveEdited(config) else onStart(config) },
        ctaLabel = if (onSaveEdited != null) "SALVA MODIFICHE" else null,
        showBookmark = onSaveEdited == null,
        onSaveAsWod = if (onSaveEdited == null) { name, desc ->
            scope.launch { wodApp.savedWodRepository.saveFromConfig(name, desc, config) }
        } else null,
        initialWodName = initialWodName,
        initialWodDesc = initialWodDesc,
        onRenameWod = onRenameWod,
    ) {
        Spacer(Modifier.height(WodTheme.spacing.l))

        // ── Section header + Add button ──────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "INTERVALLI",
                style = WodTheme.typography.labelSmall,
                color = colors.textDisabled,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = {
                intervals = intervals + CustomInterval("", 30)
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Aggiungi intervallo",
                    tint = colors.accentCustom,
                )
            }
        }

        Spacer(Modifier.height(WodTheme.spacing.s))

        HorizontalDivider(color = colors.divider)

        // ── Interval rows ────────────────────────────────────────────────────
        intervals.forEachIndexed { index, interval ->
            IntervalRow(
                index       = index,
                interval    = interval,
                canDelete   = intervals.size > 1,
                onNameChange     = { name ->
                    intervals = intervals.toMutableList().also { it[index] = it[index].copy(name = name) }
                },
                onDurationChange = { secs ->
                    intervals = intervals.toMutableList().also { it[index] = it[index].copy(durationSeconds = secs) }
                },
                onDelete = {
                    intervals = intervals.toMutableList().also { it.removeAt(index) }
                },
            )
            HorizontalDivider(color = colors.divider)
        }

        Spacer(Modifier.height(WodTheme.spacing.l))

        WodRepeatBlock(
            config = repeat,
            onConfigChange = { repeat = it },
        )

        Spacer(Modifier.height(WodTheme.spacing.l))

        NotesField(value = notes, onValueChange = { notes = it })

        Spacer(Modifier.height(WodTheme.spacing.l))
    }
}

// ── Interval row ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntervalRow(
    index: Int,
    interval: CustomInterval,
    canDelete: Boolean,
    onNameChange: (String) -> Unit,
    onDurationChange: (Int) -> Unit,
    onDelete: () -> Unit,
) {
    val colors = WodTheme.colors
    val typography = WodTheme.typography

    // Bottom-sheet state for the inline time picker
    var showSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var tempMinutes by remember(showSheet, interval.durationSeconds) {
        mutableIntStateOf(interval.durationSeconds / 60)
    }
    var tempSeconds by remember(showSheet, interval.durationSeconds) {
        mutableIntStateOf(interval.durationSeconds % 60)
    }
    val minuteItems = remember { (0..99).map { "%02d".format(it) } }
    val secondItems = remember { (0..59).map { "%02d".format(it) } }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WodTheme.spacing.s),
    ) {
        // Index badge
        Text(
            text = "${index + 1}",
            style = typography.labelSmall,
            color = colors.textDisabled,
            modifier = Modifier.width(20.dp),
            textAlign = TextAlign.End,
        )

        // Name field
        OutlinedTextField(
            value = interval.name,
            onValueChange = onNameChange,
            placeholder = {
                Text(
                    "Nome esercizio",
                    style = typography.bodyMedium,
                    color = colors.textDisabled,
                )
            },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next,
            ),
            textStyle = typography.bodyMedium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor     = colors.textPrimary,
                unfocusedTextColor   = colors.textPrimary,
                focusedBorderColor   = colors.accentCustom,
                unfocusedBorderColor = colors.divider,
                focusedLabelColor    = colors.accentCustom,
                unfocusedLabelColor  = colors.textSecondary,
                cursorColor          = colors.accentCustom,
            ),
        )

        // Compact time badge — tapping opens a bottom sheet
        Box(
            modifier = Modifier
                .size(width = 80.dp, height = 48.dp)
                .border(2.dp, colors.accentCustom, RoundedCornerShape(12.dp))
                .background(colors.bgSurface, RoundedCornerShape(12.dp))
                .clickable { showSheet = true },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "%02d:%02d".format(
                    interval.durationSeconds / 60,
                    interval.durationSeconds % 60,
                ),
                style = typography.titleMedium,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
            )
        }

        // Delete button — always present; disabled (ghost) when only 1 interval remains
        IconButton(
            onClick = { if (canDelete) onDelete() },
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Rimuovi intervallo",
                tint = if (canDelete) colors.error else colors.textDisabled,
                modifier = Modifier.size(20.dp),
            )
        }
    }

    // ── Inline time-picker bottom sheet ──────────────────────────────────────
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = colors.bgPrimary,
            dragHandle = { BottomSheetDefaults.DragHandle(color = colors.divider) },
        ) {
            val name = interval.name.takeIf { it.isNotBlank() } ?: "Intervallo ${index + 1}"
            Text(
                text = name,
                style = typography.titleMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WheelPicker(
                    items = minuteItems,
                    selectedIndex = tempMinutes.coerceIn(0, minuteItems.lastIndex),
                    onIndexChanged = { tempMinutes = it },
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = ":",
                    style = typography.headlineMedium,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                WheelPicker(
                    items = secondItems,
                    selectedIndex = tempSeconds.coerceIn(0, secondItems.lastIndex),
                    onIndexChanged = { tempSeconds = it },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    onDurationChange((tempMinutes * 60 + tempSeconds).coerceAtLeast(1))
                    showSheet = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accentCustom,
                    contentColor = Color.White,
                ),
            ) {
                Text("Conferma", style = typography.titleMedium)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

// T04d / T56b — preview

@WodPreview
@Composable
private fun CustomConfigScreenPreview() {
    WodTheme { CustomConfigScreen(onBack = {}, onStart = {}) }
}

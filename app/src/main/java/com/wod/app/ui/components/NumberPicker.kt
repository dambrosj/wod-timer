package com.wod.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
 * Tappable value card that opens a drum-roll bottom sheet for integer picking.
 *
 * Renders as a full-width row: [rounded card with value] [label].
 * Tapping the card opens a [ModalBottomSheet] with a [WheelPicker].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    min: Int = 1,
    max: Int = 99,
    enabled: Boolean = true,
) {
    val colors = WodTheme.colors
    val typography = WodTheme.typography

    var showSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val items = remember(min, max) { (min..max).map { it.toString() } }

    // Temp index while the sheet is visible; resets whenever sheet visibility or value changes
    var tempIdx by remember(showSheet, value, min) {
        mutableIntStateOf((value - min).coerceIn(0, items.lastIndex))
    }

    val borderColor = if (enabled) colors.accentTabata else colors.textDisabled
    val textColor = if (enabled) colors.textPrimary else colors.textDisabled

    // ── Card row ──────────────────────────────────────────────────────────────
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(68.dp)
                .border(2.dp, borderColor, RoundedCornerShape(20.dp))
                .background(colors.bgSurface, RoundedCornerShape(20.dp))
                .then(if (enabled) Modifier.clickable { showSheet = true } else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = value.toString(),
                style = typography.headlineMedium,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
        if (label.isNotEmpty()) {
            Spacer(Modifier.width(20.dp))
            Text(
                text = label,
                style = typography.headlineMedium,
                color = if (enabled) colors.textPrimary else colors.textDisabled,
            )
        }
    }

    // ── Bottom sheet ──────────────────────────────────────────────────────────
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = colors.bgPrimary,
            dragHandle = { BottomSheetDefaults.DragHandle(color = colors.divider) },
        ) {
            if (label.isNotEmpty()) {
                Text(
                    text = label,
                    style = typography.titleMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                )
            }
            WheelPicker(
                items = items,
                selectedIndex = tempIdx,
                onIndexChanged = { tempIdx = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    onValueChange(min + tempIdx)
                    showSheet = false
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


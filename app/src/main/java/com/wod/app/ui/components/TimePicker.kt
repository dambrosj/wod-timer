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
 * Tappable MM:SS card that opens a drum-roll bottom sheet with two wheels (minutes + seconds).
 *
 * Renders as a full-width row: [rounded card with MM:SS] [label].
 * Tapping opens a [ModalBottomSheet] with parallel minute and second [WheelPicker]s.
 *
 * @param totalSeconds Current value in seconds.
 * @param maxMinutes   Upper bound for the minute wheel (default 99).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePicker(
    totalSeconds: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    enabled: Boolean = true,
    maxMinutes: Int = 99,
) {
    val colors = WodTheme.colors
    val typography = WodTheme.typography

    var showSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val minuteItems = remember(maxMinutes) { (0..maxMinutes).map { "%02d".format(it) } }
    val secondItems = remember { (0..59).map { "%02d".format(it) } }

    // Temp state while sheet is visible; resets on sheet/value change
    var tempMinutes by remember(showSheet, totalSeconds) {
        mutableIntStateOf((totalSeconds / 60).coerceIn(0, maxMinutes))
    }
    var tempSeconds by remember(showSheet, totalSeconds) {
        mutableIntStateOf(totalSeconds % 60)
    }

    val displayText = "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
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
                text = displayText,
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
            // Two wheels side-by-side with a ":" separator
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
                    onValueChange((tempMinutes * 60 + tempSeconds).coerceAtLeast(1))
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


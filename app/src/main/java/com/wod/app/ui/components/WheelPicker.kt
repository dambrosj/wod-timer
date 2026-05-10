package com.wod.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wod.app.ui.theme.WodTheme
import kotlin.math.abs

private val ITEM_HEIGHT = 56.dp
private const val VISIBLE_ITEMS = 5
private const val HALF_VISIBLE = VISIBLE_ITEMS / 2 // = 2

/**
 * iOS-style drum-roll scroll picker.
 *
 * [HALF_VISIBLE] null sentinels are prepended/appended so the first and last
 * real items can scroll to the center position.
 *
 * scrollToItem(realIdx) → item realIdx lands at center.
 * firstVisibleItemIndex == centeredRealIdx at rest.
 */
@Composable
fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onIndexChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WodTheme.colors
    val typography = WodTheme.typography

    val paddedItems: List<String?> =
        List(HALF_VISIBLE) { null } + items + List(HALF_VISIBLE) { null }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedIndex.coerceIn(0, items.lastIndex),
    )
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Index of the real item currently at the centre of the viewport
    val centeredIdx by remember {
        derivedStateOf { listState.firstVisibleItemIndex.coerceIn(0, items.lastIndex) }
    }

    // Notify caller once scrolling settles
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            onIndexChanged(centeredIdx)
        }
    }

    // Sync when selectedIndex changes externally (e.g. sheet re-opens)
    LaunchedEffect(selectedIndex) {
        val clamped = selectedIndex.coerceIn(0, items.lastIndex)
        if (centeredIdx != clamped && !listState.isScrollInProgress) {
            listState.animateScrollToItem(clamped)
        }
    }

    val totalHeight = ITEM_HEIGHT * VISIBLE_ITEMS

    Box(modifier = modifier.height(totalHeight)) {

        // ── Drum list ────────────────────────────────────────────────────────
        LazyColumn(
            state = listState,
            flingBehavior = snapBehavior,
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            itemsIndexed(paddedItems) { paddedIndex, item ->
                val realIdx = paddedIndex - HALF_VISIBLE
                val absD = abs(realIdx - centeredIdx)

                val itemAlpha = when (absD) {
                    0 -> 1f
                    1 -> 0.40f
                    else -> 0.15f
                }
                val itemStyle = when (absD) {
                    0 -> typography.headlineMedium
                    1 -> typography.bodyLarge
                    else -> typography.bodyMedium
                }
                val itemColor = if (absD == 0) colors.textPrimary else colors.textSecondary

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ITEM_HEIGHT),
                    contentAlignment = Alignment.Center,
                ) {
                    if (item != null) {
                        Text(
                            text = item,
                            style = itemStyle,
                            color = itemColor,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier.graphicsLayer { alpha = itemAlpha },
                        )
                    }
                }
            }
        }

        // ── Gradient fade at edges ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeight)
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to colors.bgPrimary,
                            0.28f to Color.Transparent,
                            0.72f to Color.Transparent,
                            1.00f to colors.bgPrimary,
                        ),
                    ),
                ),
        )

        // ── Selection indicator ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.8f)
                .height(ITEM_HEIGHT)
                .border(
                    width = 1.dp,
                    color = colors.accentTabata.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(12.dp),
                ),
        )
    }
}

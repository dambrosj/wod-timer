package com.wod.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Spacing tokens — PRD §9.3. */
data class WodSpacing(
    val xs: Dp = 4.dp,
    val s: Dp = 8.dp,
    val m: Dp = 16.dp,
    val l: Dp = 24.dp,
    val xl: Dp = 32.dp,
)

val WodDefaultSpacing = WodSpacing()

/** Stroke widths — PRD §9.3. */
data class WodStrokes(
    val ring: Dp = 4.dp,
    val arc: Dp = 8.dp,
)

val WodDefaultStrokes = WodStrokes()

/** Shape tokens — PRD §9.3. */
data class WodShapes(
    val small: RoundedCornerShape = RoundedCornerShape(8.dp),
    val medium: RoundedCornerShape = RoundedCornerShape(16.dp),
    val pill: RoundedCornerShape = RoundedCornerShape(999.dp),
)

val WodDefaultShapes = WodShapes()

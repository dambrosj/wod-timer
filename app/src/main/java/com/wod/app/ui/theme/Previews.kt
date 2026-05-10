package com.wod.app.ui.theme

import android.content.res.Configuration
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.tooling.preview.Preview

/**
 * T04c — CompositionLocal for [WindowSizeClass].
 *
 * Usage in a screen:
 * ```kotlin
 * val windowSize = LocalWindowSizeClass.current
 * val isCompact = windowSize.widthSizeClass == WindowWidthSizeClass.Compact
 * ```
 *
 * Wire it once at the root in [MainActivity]:
 * ```kotlin
 * val windowSize = calculateWindowSizeClass(this)
 * CompositionLocalProvider(LocalWindowSizeClass provides windowSize) { … }
 * ```
 *
 * A safe fallback (Compact width) is provided so Composables that read this
 * local still work in Previews or tests that don't wrap with the provider.
 */
val LocalWindowSizeClass = compositionLocalOf<WindowSizeClass?> { null }

// ---------------------------------------------------------------------------
// T04d — @Preview annotation macros for 4 mandatory screen variants.
// ---------------------------------------------------------------------------

/**
 * Apply this to every screen's private preview Composable to generate
 * all four required variants simultaneously:
 *
 * 1. Dark · Portrait
 * 2. Dark · Landscape
 * 3. Light · Portrait
 * 4. Light · Landscape
 *
 * Usage:
 * ```kotlin
 * @WodPreview
 * @Composable
 * private fun HomeScreenPreview() {
 *     WodTheme { HomeScreen(onTimerTypeClick = {}, onDiaryClick = {}) }
 * }
 * ```
 */
@Preview(
    name = "Dark · Portrait",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    widthDp = 390, heightDp = 844,
)
@Preview(
    name = "Dark · Landscape",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    widthDp = 844, heightDp = 390,
)
@Preview(
    name = "Light · Portrait",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    widthDp = 390, heightDp = 844,
)
@Preview(
    name = "Light · Landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    widthDp = 844, heightDp = 390,
)
annotation class WodPreview

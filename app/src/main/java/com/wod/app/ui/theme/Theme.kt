package com.wod.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/** User-selectable theme mode (persisted in DataStore as part of T04a). */
enum class ThemeMode { DARK, LIGHT, AUTO }

val LocalWodColors = staticCompositionLocalOf { WodDarkColors }
val LocalWodTypography = staticCompositionLocalOf { WodDefaultTypography }
val LocalWodSpacing = staticCompositionLocalOf { WodDefaultSpacing }
val LocalWodShapes = staticCompositionLocalOf { WodDefaultShapes }
val LocalWodStrokes = staticCompositionLocalOf { WodDefaultStrokes }

/**
 * Convenience accessor: `WodTheme.colors`, `WodTheme.typography`, etc.
 * Mirrors the `MaterialTheme` ergonomics so call-sites stay short.
 */
object WodTheme {
    val colors: WodColors
        @Composable get() = LocalWodColors.current
    val typography: WodTypography
        @Composable get() = LocalWodTypography.current
    val spacing: WodSpacing
        @Composable get() = LocalWodSpacing.current
    val shapes: WodShapes
        @Composable get() = LocalWodShapes.current
    val strokes: WodStrokes
        @Composable get() = LocalWodStrokes.current
}

/**
 * Root theme. Default is [ThemeMode.DARK] per PRD §9.5.
 *
 * Wrap the whole app once in [MainActivity] and read the user's choice from
 * DataStore — passing it in causes a recomposition that swaps the palette
 * across all open screens (live theme switching, T46b).
 */
@Composable
fun WodTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.AUTO -> systemDark
    }
    val colors = if (isDark) WodDarkColors else WodLightColors
    val typography = WodDefaultTypography

    val materialColors = if (isDark) {
        darkColorScheme(
            primary = colors.accentTabata,
            background = colors.bgPrimary,
            surface = colors.bgSurface,
            onPrimary = colors.textPrimary,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary,
            error = colors.error,
        )
    } else {
        lightColorScheme(
            primary = colors.accentTabata,
            background = colors.bgPrimary,
            surface = colors.bgSurface,
            onPrimary = colors.textPrimary,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary,
            error = colors.error,
        )
    }

    val materialTypography = Typography(
        displayLarge = typography.displayCountdown,
        displayMedium = typography.displayTimer,
        displaySmall = typography.displayLogo,
        headlineLarge = typography.headlineLarge,
        headlineMedium = typography.headlineMedium,
        titleMedium = typography.titleMedium,
        bodyLarge = typography.bodyLarge,
        bodyMedium = typography.bodyMedium,
        labelSmall = typography.labelSmall,
    )

    CompositionLocalProvider(
        LocalWodColors provides colors,
        LocalWodTypography provides typography,
        LocalWodSpacing provides WodDefaultSpacing,
        LocalWodShapes provides WodDefaultShapes,
        LocalWodStrokes provides WodDefaultStrokes,
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = materialTypography,
            content = content,
        )
    }
}

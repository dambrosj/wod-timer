package com.wod.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Type styles defined in PRD §9.2.
 *
 * Family: `Outfit` (variable, OFL). Bundle the variable font under
 * res/font/outfit_variable.ttf and replace [WodFontFamily] below to use it.
 * For now we fall back to FontFamily.SansSerif so the project builds without
 * the font file.
 */
val WodFontFamily: FontFamily = FontFamily.SansSerif

data class WodTypography(
    val displayLogo: TextStyle,
    val displayTimer: TextStyle,
    val displayCountdown: TextStyle,
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val titleMedium: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val labelSmall: TextStyle,
    val exerciseLarge: TextStyle,
)

val WodDefaultTypography = WodTypography(
    displayLogo = TextStyle(
        fontFamily = WodFontFamily,
        fontWeight = FontWeight.ExtraLight,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = 4.sp,
    ),
    displayTimer = TextStyle(
        fontFamily = WodFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 96.sp,
        lineHeight = 96.sp,
        // Tabular numerals so digits don't jiggle as they tick.
        fontFeatureSettings = "tnum",
    ),
    displayCountdown = TextStyle(
        fontFamily = WodFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 160.sp,
        lineHeight = 160.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = WodFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 40.sp,
        letterSpacing = 1.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = WodFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = WodFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = WodFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = WodFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = WodFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.sp,
    ),
    exerciseLarge = TextStyle(
        fontFamily = WodFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = 2.sp,
    ),
)

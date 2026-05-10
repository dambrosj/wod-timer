package com.wod.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Semantic color tokens. The values here mirror the table in PRD §9.1.
 *
 * Brand accents (the 5 timer types + premium badge) are intentionally
 * theme-invariant: they keep their identity in both dark and light modes.
 *
 * Phase colors (work/rest/wodRest) are slightly desaturated in light theme
 * to preserve AA contrast on the lighter background.
 */
data class WodColors(
    val bgPrimary: Color,
    val bgSurface: Color,
    val bgElevated: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val iconDefault: Color,
    val accentAmrap: Color,
    val accentForTime: Color,
    val accentEmom: Color,
    val accentTabata: Color,
    val accentMix: Color,
    val phaseWork: Color,
    val phaseRest: Color,
    val phaseWodRest: Color,
    val ringDashes: Color,
    val premiumBadge: Color,
    val success: Color,
    val error: Color,
    val isDark: Boolean,
)

val WodDarkColors = WodColors(
    bgPrimary       = Color(0xFF000000),
    bgSurface       = Color(0xFF0E0E0E),
    bgElevated      = Color(0xFF1A1A1A),
    divider         = Color(0xFF2A2A2A),
    textPrimary     = Color(0xFFFFFFFF),
    textSecondary   = Color(0xFFAAAAAA),
    textDisabled    = Color(0xFF555555),
    iconDefault     = Color(0xFFFFFFFF),
    // ── Accents (new palette, distinct from SmartWOD) ─────────────────────
    accentAmrap     = Color(0xFFD62839),   // cremisi
    accentForTime   = Color(0xFF0FA3B1),   // cyan oceano
    accentEmom      = Color(0xFFF5A623),   // ambra/oro
    accentTabata    = Color(0xFF4361EE),   // indaco elettrico
    accentMix       = Color(0xFF5C677D),   // slate blu (I MIEI WOD)
    // ── Phase colors ──────────────────────────────────────────────────────
    phaseWork       = Color(0xFF4361EE),   // indaco (matching Tabata)
    phaseRest       = Color(0xFF0FA3B1),   // cyan (matching ForTime)
    phaseWodRest    = Color(0xFFD62839),   // cremisi (matching Amrap)
    ringDashes      = Color(0xFF444444),
    premiumBadge    = Color(0xFFF5A623),
    success         = Color(0xFF2DC653),
    error           = Color(0xFFE55353),
    isDark          = true,
)

val WodLightColors = WodColors(
    bgPrimary       = Color(0xFFF4F4F4),
    bgSurface       = Color(0xFFFFFFFF),
    bgElevated      = Color(0xFFFFFFFF),
    divider         = Color(0xFFE2E2E2),
    textPrimary     = Color(0xFF0E0E0E),
    textSecondary   = Color(0xFF5F5F5F),
    textDisabled    = Color(0xFFB0B0B0),
    iconDefault     = Color(0xFF0E0E0E),
    // ── Accents — leggermente scurite per leggibilità su sfondo chiaro ──
    accentAmrap     = Color(0xFFB8202D),
    accentForTime   = Color(0xFF0A7985),
    accentEmom      = Color(0xFFBF7E10),
    accentTabata    = Color(0xFF2D47CC),
    accentMix       = Color(0xFF4A5568),
    // ── Phase colors ──────────────────────────────────────────────────────
    phaseWork       = Color(0xFF2D47CC),
    phaseRest       = Color(0xFF0A7985),
    phaseWodRest    = Color(0xFFB8202D),
    ringDashes      = Color(0xFFC8C8C8),
    premiumBadge    = Color(0xFFBF7E10),
    success         = Color(0xFF1E9A45),
    error           = Color(0xFFD63A3A),
    isDark          = false,
)

package com.wod.app.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wod.app.R
import com.wod.app.domain.model.TimerType
import com.wod.app.ui.components.TimerTypeButton
import com.wod.app.ui.theme.WodPreview
import com.wod.app.ui.theme.WodTheme

/**
 * Home screen: 4 timer-type pill buttons + "I miei WOD" shortcut.
 *
 * Portrait  → 2+2+2 grid: [AMRAP|FOR TIME] / [EMOM|TABATA] / [CUSTOM|I MIEI WOD].
 * Landscape → row of 4 + [CUSTOM|I MIEI WOD] below.
 * Hamburger → [onMenuClick].
 */
@Composable
fun HomeScreen(
    onTimerTypeClick: (TimerType) -> Unit,
    onDiaryClick: () -> Unit,
    onMyWodsClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
) {
    val colors = WodTheme.colors
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary),
    ) {
        // Top bar: hamburger (left) + logo (center) + bell (right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = WodTheme.spacing.m, vertical = WodTheme.spacing.m)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = colors.iconDefault,
                modifier = Modifier
                    .size(28.dp)
                    .clickable(onClick = onMenuClick),
            )
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = colors.iconDefault,
                modifier = Modifier.size(28.dp),
            )
        }

        // Central content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = WodTheme.spacing.l,
                    vertical = if (isLandscape) WodTheme.spacing.s else WodTheme.spacing.xl,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (!isLandscape) {
                Text(
                    text = stringResource(R.string.home_title),
                    style = WodTheme.typography.displayLogo,
                    color = colors.textPrimary,
                )
                Text(
                    text = stringResource(R.string.home_subtitle),
                    style = WodTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(WodTheme.spacing.l))
            }

            if (isLandscape) {
                LandscapeButtonGrid(
                    onTimerTypeClick = onTimerTypeClick,
                    onMyWodsClick = onMyWodsClick,
                )
            } else {
                PortraitButtonColumn(
                    onTimerTypeClick = onTimerTypeClick,
                    onMyWodsClick = onMyWodsClick,
                )
            }

            Spacer(Modifier.height(WodTheme.spacing.l))
            Text(
                text = stringResource(R.string.home_diary_link),
                style = WodTheme.typography.titleMedium,
                color = colors.textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDiaryClick)
                    .padding(WodTheme.spacing.m),
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Portrait: [AMRAP|FOR TIME] / [EMOM|TABATA] / [CUSTOM|I MIEI WOD] */
@Composable
private fun PortraitButtonColumn(
    onTimerTypeClick: (TimerType) -> Unit,
    onMyWodsClick: () -> Unit,
) {
    val colors = WodTheme.colors
    val gap = WodTheme.spacing.m
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap),
        ) {
            Box(Modifier.weight(1f)) {
                TimerTypeButton("AMRAP",    colors.accentAmrap)   { onTimerTypeClick(TimerType.AMRAP) }
            }
            Box(Modifier.weight(1f)) {
                TimerTypeButton("FOR TIME", colors.accentForTime) { onTimerTypeClick(TimerType.FOR_TIME) }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap),
        ) {
            Box(Modifier.weight(1f)) {
                TimerTypeButton("EMOM",   colors.accentEmom)   { onTimerTypeClick(TimerType.EMOM) }
            }
            Box(Modifier.weight(1f)) {
                TimerTypeButton("TABATA", colors.accentTabata) { onTimerTypeClick(TimerType.TABATA) }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap),
        ) {
            Box(Modifier.weight(1f)) {
                TimerTypeButton("CUSTOM", colors.accentCustom) { onTimerTypeClick(TimerType.CUSTOM) }
            }
            Box(Modifier.weight(1f)) {
                TimerTypeButton("I MIEI WOD", colors.accentMix) { onMyWodsClick() }
            }
        }
    }
}

/** Landscape: [AMRAP|FOR TIME|EMOM|TABATA] / [CUSTOM|I MIEI WOD] */
@Composable
private fun LandscapeButtonGrid(
    onTimerTypeClick: (TimerType) -> Unit,
    onMyWodsClick: () -> Unit,
) {
    val colors = WodTheme.colors
    val gap = WodTheme.spacing.m
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap),
        ) {
            Box(Modifier.weight(1f)) {
                TimerTypeButton("AMRAP",    colors.accentAmrap)   { onTimerTypeClick(TimerType.AMRAP) }
            }
            Box(Modifier.weight(1f)) {
                TimerTypeButton("FOR TIME", colors.accentForTime) { onTimerTypeClick(TimerType.FOR_TIME) }
            }
            Box(Modifier.weight(1f)) {
                TimerTypeButton("EMOM",     colors.accentEmom)    { onTimerTypeClick(TimerType.EMOM) }
            }
            Box(Modifier.weight(1f)) {
                TimerTypeButton("TABATA",   colors.accentTabata)  { onTimerTypeClick(TimerType.TABATA) }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap),
        ) {
            Box(Modifier.weight(1f)) {
                TimerTypeButton("CUSTOM", colors.accentCustom) { onTimerTypeClick(TimerType.CUSTOM) }
            }
            Box(Modifier.weight(1f)) {
                TimerTypeButton("I MIEI WOD", colors.accentMix) { onMyWodsClick() }
            }
        }
    }
}


// T04d / T56b — 4-variant previews

@WodPreview
@Composable
private fun HomeScreenPreview() {
    WodTheme {
        HomeScreen(onTimerTypeClick = {}, onDiaryClick = {})
    }
}

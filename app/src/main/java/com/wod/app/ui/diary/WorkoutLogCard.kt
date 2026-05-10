package com.wod.app.ui.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wod.app.domain.model.TimerType
import com.wod.app.domain.model.WorkoutLog
import com.wod.app.ui.theme.WodColors
import com.wod.app.ui.theme.WodTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Card showing a summary row for a single [WorkoutLog] entry. */
@Composable
fun WorkoutLogCard(log: WorkoutLog, onClick: () -> Unit) {
    val colors = WodTheme.colors
    val typography = WodTheme.typography
    val spacing = WodTheme.spacing
    val shapes = WodTheme.shapes
    val accent = log.type.accentColor(colors)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgSurface, shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.m, vertical = spacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Colored accent bar on the left
        Spacer(
            modifier = Modifier
                .width(4.dp)
                .height(56.dp)
                .background(accent, shapes.pill),
        )
        Spacer(Modifier.width(spacing.m))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.type.displayName(),
                style = typography.titleMedium,
                color = accent,
            )
            Text(
                text = formatDate(log.completedAt),
                style = typography.bodyMedium,
                color = colors.textSecondary,
            )
            if (log.notes.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = log.notes,
                    style = typography.labelSmall,
                    color = colors.textDisabled,
                    maxLines = 1,
                )
            }
        }
        Spacer(Modifier.width(spacing.s))
        Text(
            text = formatDuration(log.durationSeconds),
            style = typography.headlineMedium,
            color = colors.textPrimary,
        )
    }
}

internal fun formatDate(ms: Long): String =
    SimpleDateFormat("d MMM yyyy  HH:mm", Locale.getDefault()).format(Date(ms))

internal fun formatDuration(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

internal fun TimerType.displayName(): String = when (this) {
    TimerType.AMRAP    -> "AMRAP"
    TimerType.FOR_TIME -> "FOR TIME"
    TimerType.EMOM     -> "EMOM"
    TimerType.TABATA   -> "TABATA"
    TimerType.MIX      -> "MIX"
}

internal fun TimerType.accentColor(colors: WodColors) = when (this) {
    TimerType.AMRAP    -> colors.accentAmrap
    TimerType.FOR_TIME -> colors.accentForTime
    TimerType.EMOM     -> colors.accentEmom
    TimerType.TABATA   -> colors.accentTabata
    TimerType.MIX      -> colors.accentMix
}

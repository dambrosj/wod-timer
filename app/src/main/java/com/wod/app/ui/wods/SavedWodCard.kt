package com.wod.app.ui.wods

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wod.app.domain.model.SavedWod
import com.wod.app.domain.model.TimerType
import com.wod.app.ui.theme.WodColors
import com.wod.app.ui.theme.WodTheme

/** Card showing a single [SavedWod] entry with quick-actions (T44b, T44c). */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SavedWodCard(
    wod: SavedWod,
    onClick: () -> Unit,
    onAvvia: () -> Unit,
    onToggleFavourite: () -> Unit,
    onDuplicate: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = WodTheme.colors
    val typography = WodTheme.typography
    val spacing = WodTheme.spacing
    val shapes = WodTheme.shapes
    val accent = wod.type.accentColor(colors)

    var showContextMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgSurface, shapes.medium)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showContextMenu = true },
            )
            .padding(spacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left accent bar
        Spacer(
            modifier = Modifier
                .width(4.dp)
                .height(52.dp)
                .background(accent, shapes.pill),
        )
        Spacer(Modifier.width(spacing.m))

        // Name + description / exercise preview
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Type badge
                TypeBadge(wod.type, colors)
                if (wod.isBuiltIn) {
                    Spacer(Modifier.width(spacing.s))
                    Text(
                        text = "BUILT-IN",
                        style = typography.labelSmall,
                        color = colors.textDisabled,
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = wod.name,
                style = typography.titleMedium,
                color = colors.textPrimary,
                maxLines = 1,
            )
            if (wod.description.isNotBlank()) {
                Text(
                    text = wod.description,
                    style = typography.bodyMedium,
                    color = colors.textSecondary,
                    maxLines = 2,
                )
            }
            if (wod.timesUsed > 0) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Usato ${wod.timesUsed}×",
                    style = typography.labelSmall,
                    color = colors.textDisabled,
                )
            }
        }

        // Favourite
        IconButton(onClick = onToggleFavourite, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = if (wod.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (wod.isFavourite) "Rimuovi dai preferiti" else "Aggiungi ai preferiti",
                tint = if (wod.isFavourite) colors.error else colors.textDisabled,
                modifier = Modifier.size(20.dp),
            )
        }

        // AVVIA
        Button(
            onClick = onAvvia,
            colors = ButtonDefaults.buttonColors(
                containerColor = accent,
                contentColor = colors.bgPrimary,
            ),
            modifier = Modifier.height(36.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp),
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("AVVIA", style = typography.labelSmall)
        }

        // Long-press context menu (T44c)
        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false },
            containerColor = colors.bgElevated,
        ) {
            DropdownMenuItem(
                text = { Text("Duplica", color = colors.textPrimary) },
                leadingIcon = { Icon(Icons.Default.ContentCopy, null, tint = colors.textSecondary) },
                onClick = { showContextMenu = false; onDuplicate() },
            )
            if (!wod.isBuiltIn) {
                DropdownMenuItem(
                    text = { Text("Modifica", color = colors.textPrimary) },
                    leadingIcon = { Icon(Icons.Default.Edit, null, tint = colors.textSecondary) },
                    onClick = { showContextMenu = false; onEdit() },
                )
                DropdownMenuItem(
                    text = { Text("Elimina", color = colors.error) },
                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = colors.error) },
                    onClick = { showContextMenu = false; onDelete() },
                )
            }
        }
    }
}

@Composable
internal fun TypeBadge(type: TimerType, colors: WodColors) {
    val accent = type.accentColor(colors)
    Text(
        text = type.displayName(),
        style = WodTheme.typography.labelSmall,
        color = accent,
        modifier = Modifier
            .background(accent.copy(alpha = 0.15f), WodTheme.shapes.pill)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

internal fun TimerType.displayName(): String = when (this) {
    TimerType.AMRAP    -> "AMRAP"
    TimerType.FOR_TIME -> "FOR TIME"
    TimerType.EMOM     -> "EMOM"
    TimerType.TABATA   -> "TABATA"
    TimerType.MIX      -> "MIX"
    TimerType.CUSTOM   -> "CUSTOM"
}

internal fun TimerType.accentColor(colors: WodColors) = when (this) {
    TimerType.AMRAP    -> colors.accentAmrap
    TimerType.FOR_TIME -> colors.accentForTime
    TimerType.EMOM     -> colors.accentEmom
    TimerType.TABATA   -> colors.accentTabata
    TimerType.MIX      -> colors.accentMix
    TimerType.CUSTOM   -> colors.accentCustom
}

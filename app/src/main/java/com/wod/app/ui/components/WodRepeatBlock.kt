package com.wod.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wod.app.R
import com.wod.app.domain.model.WodRepeatConfig
import com.wod.app.ui.theme.WodTheme

/**
 * Collapsible "Ripetizioni WOD" section shared by all config screens (T21b).
 *
 * - Collapsed by default.
 * - Exposes [Round WOD] (NumberPicker) and [Riposo tra round] (TimePicker).
 * - [Riposo tra round] is disabled when `config.wodRounds == 1`.
 * - Shows helper text below when expanded.
 *
 * @param config  Current [WodRepeatConfig] value.
 * @param onConfigChange  Called with the updated config on any change.
 */
@Composable
fun WodRepeatBlock(
    config: WodRepeatConfig,
    onConfigChange: (WodRepeatConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WodTheme.colors
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(color = colors.divider)

        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = WodTheme.spacing.m, horizontal = WodTheme.spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.config_wod_repeat_title),
                style = WodTheme.typography.bodyLarge,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = colors.textSecondary,
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bgSurface)
                    .padding(WodTheme.spacing.m),
                verticalArrangement = Arrangement.spacedBy(WodTheme.spacing.m),
            ) {
                NumberPicker(
                    value = config.wodRounds,
                    onValueChange = { onConfigChange(config.copy(wodRounds = it)) },
                    label = stringResource(R.string.config_wod_rounds),
                    min = 1,
                    max = 20,
                )
                TimePicker(
                    totalSeconds = config.restBetweenRoundsSeconds,
                    onValueChange = { onConfigChange(config.copy(restBetweenRoundsSeconds = it)) },
                    label = stringResource(R.string.config_rest_between),
                    enabled = config.wodRounds > 1,
                )

                val rounds = config.wodRounds
                val rest = config.restBetweenRoundsSeconds
                val restStr = "%d:%02d".format(rest / 60, rest % 60)
                Text(
                    text = "Il WOD verrà ripetuto $rounds volte con $restStr di riposo tra una ripetizione e la successiva",
                    style = WodTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )

                Spacer(Modifier.height(WodTheme.spacing.xs))
            }
        }

        HorizontalDivider(color = colors.divider)
    }
}

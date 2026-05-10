package com.wod.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wod.app.R
import com.wod.app.ui.theme.WodTheme

/**
 * Drawer content composable for the app-wide [ModalNavigationDrawer].
 *
 * Items:
 * - Timer → home
 * - I miei WOD → wods
 * - Diario del Workout → diary
 * - Impostazioni e aiuto → settings
 *
 * [currentRoute] is used to highlight the active item.
 */
@Composable
fun WodDrawerContent(
    currentRoute: String?,
    onTimer: () -> Unit,
    onMyWods: () -> Unit,
    onDiary: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WodTheme.colors
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(colors.bgSurface)
            .padding(vertical = WodTheme.spacing.xl),
    ) {
        // App logo / brand
        Text(
            text = "wod",
            style = WodTheme.typography.displayLogo,
            color = colors.textPrimary,
            modifier = Modifier.padding(
                start = WodTheme.spacing.l,
                bottom = WodTheme.spacing.l,
            ),
        )

        HorizontalDivider(color = colors.divider)
        Spacer(Modifier.height(WodTheme.spacing.m))

        DrawerItem(
            icon = Icons.Default.Timer,
            label = stringResource(R.string.drawer_timer),
            selected = currentRoute == "home",
            onClick = onTimer,
        )
        DrawerItem(
            icon = Icons.Default.Bookmark,
            label = stringResource(R.string.drawer_my_wods),
            selected = currentRoute == "wods",
            onClick = onMyWods,
        )
        DrawerItem(
            icon = Icons.AutoMirrored.Filled.List,
            label = stringResource(R.string.drawer_diary),
            selected = currentRoute == "diary",
            onClick = onDiary,
        )
        DrawerItem(
            icon = Icons.Default.Settings,
            label = stringResource(R.string.drawer_settings),
            selected = currentRoute == "settings",
            onClick = onSettings,
        )

        Spacer(Modifier.weight(1f))

        HorizontalDivider(color = colors.divider)
        Spacer(Modifier.height(WodTheme.spacing.m))
        Text(
            text = "Powered by Nicola D'Ambrosio",
            style = WodTheme.typography.labelSmall,
            color = colors.textDisabled,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = WodTheme.spacing.s),
        )
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = WodTheme.colors
    val bg = if (selected) colors.bgElevated else colors.bgSurface
    val tint = if (selected) colors.textPrimary else colors.textSecondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = WodTheme.spacing.m)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = WodTheme.spacing.m, vertical = WodTheme.spacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(WodTheme.spacing.m))
        Text(
            text = label,
            style = WodTheme.typography.bodyLarge,
            color = tint,
        )
    }
}

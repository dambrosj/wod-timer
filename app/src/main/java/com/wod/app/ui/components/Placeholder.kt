package com.wod.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.wod.app.ui.theme.WodTheme

/**
 * Shared placeholder used by all screens that are not yet implemented.
 * Renders the screen title, a back arrow and a "Coming next…" body — so the
 * nav graph is fully clickable end-to-end before the screens are filled in.
 */
@Composable
fun PlaceholderScreen(
    title: String,
    onBack: (() -> Unit)? = null,
    body: String = "Coming next — see PRD §5.",
) {
    val colors = WodTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary)
            .padding(WodTheme.spacing.m),
    ) {
        Box(modifier = Modifier.padding(bottom = WodTheme.spacing.m)) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = colors.iconDefault,
                    )
                }
            }
        }
        Text(
            text = title,
            style = WodTheme.typography.headlineLarge,
            color = colors.textPrimary,
        )
        Box(modifier = Modifier.padding(top = WodTheme.spacing.m)) {
            Text(
                text = body,
                style = WodTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
        }
    }
}

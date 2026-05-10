package com.wod.app.ui.config

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wod.app.R
import com.wod.app.ui.theme.WodTheme

/**
 * Shared scaffold for every timer config screen (T21).
 *
 * @param title         Timer type name, e.g. "TABATA".
 * @param totalSeconds  Live total time calculation for the subtitle on the CTA.
 * @param onBack        Back arrow tap callback.
 * @param onStart       CTA button tap callback.
 * @param ctaLabel      Override the CTA button text; null uses the default string resource.
 * @param showBookmark  Show the bookmark icon (false in edit mode).
 * @param onSaveAsWod   If non-null, the bookmark icon triggers a save-as-WOD dialog.
 * @param content       Config-specific pickers and blocks.
 */
@Composable
fun ConfigScaffold(
    title: String,
    totalSeconds: Int,
    onBack: () -> Unit,
    onStart: () -> Unit,
    ctaLabel: String? = null,
    showBookmark: Boolean = true,
    onSaveAsWod: ((name: String, description: String) -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = WodTheme.colors
    var showSaveDialog by remember { mutableStateOf(false) }

    if (showSaveDialog) {
        SaveWodDialog(
            onDismiss = { showSaveDialog = false },
            onConfirm = { name, desc ->
                showSaveDialog = false
                onSaveAsWod?.invoke(name, desc)
            },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 96.dp), // leave room for pinned CTA
        ) {
            // ── Top bar ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = WodTheme.spacing.s,
                        vertical = WodTheme.spacing.s,
                    ),
            ) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Indietro",
                        tint = colors.iconDefault,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Text(
                    text = title,
                    style = WodTheme.typography.headlineMedium,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center),
                )
                IconButton(onClick = { showSaveDialog = true }, modifier = Modifier.align(Alignment.CenterEnd)) {
                    if (showBookmark) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Preset",
                            tint = colors.iconDefault,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }

            // ── Scrollable content ───────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = WodTheme.spacing.l),
                content = content,
            )
        }

        // ── Pinned CTA ───────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(colors.bgPrimary)
                .navigationBarsPadding()
                .padding(horizontal = WodTheme.spacing.l, vertical = WodTheme.spacing.m),
        ) {
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accentTabata,
                    contentColor = Color.White,
                ),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = ctaLabel ?: stringResource(R.string.config_start_timer),
                        style = WodTheme.typography.titleMedium,
                    )
                    if (totalSeconds > 0 && ctaLabel == null) {
                        Text(
                            text = stringResource(
                                R.string.config_total_time,
                                "%d:%02d".format(totalSeconds / 60, totalSeconds % 60),
                            ),
                            style = WodTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
    }
}

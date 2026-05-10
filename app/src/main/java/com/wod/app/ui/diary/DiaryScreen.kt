package com.wod.app.ui.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wod.app.domain.model.WorkoutLog
import com.wod.app.ui.theme.WodPreview
import com.wod.app.ui.theme.WodTheme

/** Workout history list (T41-T43). */
@Composable
fun DiaryScreen(
    onBack: () -> Unit,
    onLogClick: (WorkoutLog) -> Unit = {},
) {
    val vm: DiaryViewModel = viewModel()
    val history by vm.history.collectAsStateWithLifecycle()
    val colors = WodTheme.colors
    val typography = WodTheme.typography
    val spacing = WodTheme.spacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.s, vertical = spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Indietro",
                    tint = colors.textPrimary,
                )
            }
            Text(
                text = "Diario",
                style = typography.headlineMedium,
                color = colors.textPrimary,
            )
        }

        if (history.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = colors.textDisabled,
                        modifier = Modifier.size(64.dp),
                    )
                    Spacer(Modifier.height(spacing.m))
                    Text(
                        text = "Nessun allenamento\nregistrato",
                        style = typography.bodyLarge,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(spacing.m),
                verticalArrangement = Arrangement.spacedBy(spacing.s),
            ) {
                items(history, key = { it.id }) { log ->
                    WorkoutLogCard(
                        log = log,
                        onClick = {
                            vm.selectLog(log)
                            onLogClick(log)
                        },
                    )
                }
            }
        }
    }
}

// T04d / T56b — 4-variant previews

@WodPreview
@androidx.compose.runtime.Composable
private fun DiaryScreenPreview() {
    WodTheme { DiaryScreen(onBack = {}) }
}


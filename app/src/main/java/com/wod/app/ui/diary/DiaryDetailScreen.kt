package com.wod.app.ui.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wod.app.ui.theme.WodTheme

/** Detail view for a single diary entry — shows stats, editable notes, delete (T43). */
@Composable
fun DiaryDetailScreen(
    onBack: () -> Unit,
    onDeleted: () -> Unit,
) {
    val vm: DiaryDetailViewModel = viewModel()
    val log = vm.log ?: run { onBack(); return }

    val colors = WodTheme.colors
    val typography = WodTheme.typography
    val spacing = WodTheme.spacing
    val shapes = WodTheme.shapes

    var notes by rememberSaveable { mutableStateOf(log.notes) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Elimina allenamento",
                    style = typography.titleMedium,
                    color = colors.textPrimary,
                )
            },
            text = {
                Text(
                    "Sei sicuro di voler eliminare questo allenamento?",
                    color = colors.textSecondary,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { vm.delete(onDeleted) },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.error),
                ) { Text("Elimina") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annulla", color = colors.textSecondary)
                }
            },
            containerColor = colors.bgSurface,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary)
            .verticalScroll(rememberScrollState()),
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
                modifier = Modifier.weight(1f),
                text = log.type.displayName(),
                style = typography.headlineMedium,
                color = log.type.accentColor(colors),
            )
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Elimina",
                    tint = colors.error,
                )
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = spacing.m, vertical = spacing.s),
        ) {
            // Duration
            Text(
                text = formatDuration(log.durationSeconds),
                style = typography.headlineLarge.copy(fontSize = 48.sp),
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = formatDate(log.completedAt),
                style = typography.bodyMedium,
                color = colors.textSecondary,
            )

            Spacer(Modifier.height(spacing.l))

            // Notes
            Text(
                text = "Note",
                style = typography.titleMedium,
                color = colors.textSecondary,
            )
            Spacer(Modifier.height(spacing.s))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Aggiungi una nota…", color = colors.textDisabled)
                },
                minLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedBorderColor = colors.accentTabata,
                    unfocusedBorderColor = colors.divider,
                    cursorColor = colors.accentTabata,
                ),
            )
            Spacer(Modifier.height(spacing.m))
            TextButton(
                onClick = { vm.saveNotes(notes, onBack) },
                colors = ButtonDefaults.textButtonColors(contentColor = colors.accentTabata),
            ) {
                Text("Salva note", style = typography.bodyLarge)
            }
        }
    }
}

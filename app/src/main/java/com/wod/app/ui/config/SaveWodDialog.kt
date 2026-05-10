package com.wod.app.ui.config

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.wod.app.ui.theme.WodTheme

/**
 * Dialog that captures a name and optional description for saving the current
 * timer config as a named WOD in the library (T44g).
 *
 * @param onDismiss   Called when the user cancels.
 * @param onConfirm   Called with (name, description) when the user taps "Salva".
 */
@Composable
fun SaveWodDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    val colors = WodTheme.colors
    val typography = WodTheme.typography
    val spacing = WodTheme.spacing

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor    = colors.textPrimary,
        unfocusedTextColor  = colors.textPrimary,
        focusedBorderColor  = colors.accentTabata,
        unfocusedBorderColor = colors.divider,
        cursorColor         = colors.accentTabata,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = colors.bgSurface,
        title = {
            Text(
                "Salva come WOD",
                style = typography.titleMedium,
                color = colors.textPrimary,
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome *", color = colors.textSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                )
                Spacer(Modifier.height(spacing.s))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrizione (opzionale)", color = colors.textSecondary) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), description.trim()) },
                enabled = name.isNotBlank(),
                colors  = ButtonDefaults.textButtonColors(contentColor = colors.accentTabata),
            ) { Text("Salva", style = typography.titleMedium) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla", color = colors.textSecondary)
            }
        },
    )
}

package com.wod.app.ui.completion

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wod.app.domain.model.TimerType
import com.wod.app.ui.theme.WodColors
import com.wod.app.ui.theme.WodPreview
import com.wod.app.ui.theme.WodTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Post-workout summary screen (T37-T40). */
@Composable
fun CompletionScreen(onDone: () -> Unit) {
    val vm: CompletionViewModel = viewModel()
    val log = vm.workoutLog
    val colors = WodTheme.colors
    val typography = WodTheme.typography
    val spacing = WodTheme.spacing
    val shapes = WodTheme.shapes
    val context = LocalContext.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    var notes by rememberSaveable { mutableStateOf("") }

    val durationText = log?.let { formatDuration(it.durationSeconds) } ?: "--"
    val dateText = log?.let { formatDate(it.completedAt) } ?: ""
    val typeColor = log?.type?.accentColor(colors) ?: colors.accentTabata
    val typeName = log?.type?.displayName() ?: "WORKOUT"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary),
    ) {
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(spacing.m),
                horizontalArrangement = Arrangement.spacedBy(spacing.l),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left: checkmark + type
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CheckmarkCircle(colors)
                    Spacer(Modifier.height(spacing.m))
                    Text(
                        text = "COMPLETATO!",
                        style = typography.headlineLarge,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(spacing.s))
                    TypeBadge(typeName, typeColor, colors)
                }
                // Right: stats + notes + buttons
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(spacing.s),
                ) {
                    StatsSummary(durationText, dateText, colors, typography)
                    Spacer(Modifier.height(spacing.s))
                    NotesField(notes, colors, onNotes = { notes = it })
                    Spacer(Modifier.height(spacing.s))
                    ActionButtons(
                        colors = colors,
                        typography = typography,
                        onShare = { shareWorkout(context, vm.buildShareText()) },
                        onSave = { vm.save(notes, onDone) },
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.m, vertical = spacing.l),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(spacing.l))
                CheckmarkCircle(colors)
                Spacer(Modifier.height(spacing.m))
                Text(
                    text = "COMPLETATO!",
                    style = typography.headlineLarge,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(spacing.s))
                TypeBadge(typeName, typeColor, colors)
                Spacer(Modifier.height(spacing.l))
                StatsSummary(durationText, dateText, colors, typography)
                Spacer(Modifier.height(spacing.l))
                NotesField(notes, colors, onNotes = { notes = it })
                Spacer(Modifier.height(spacing.l))
                ActionButtons(
                    colors = colors,
                    typography = typography,
                    onShare = { shareWorkout(context, vm.buildShareText()) },
                    onSave = { vm.save(notes, onDone) },
                )
                Spacer(Modifier.height(spacing.l))
            }
        }
    }
}

@Composable
private fun CheckmarkCircle(colors: WodColors) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .background(colors.success, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = colors.bgPrimary,
            modifier = Modifier.size(72.dp),
        )
    }
}

@Composable
private fun TypeBadge(typeName: String, typeColor: androidx.compose.ui.graphics.Color, colors: WodColors) {
    Box(
        modifier = Modifier
            .background(typeColor.copy(alpha = 0.15f), androidx.compose.foundation.shape.RoundedCornerShape(999.dp))
            .padding(horizontal = 20.dp, vertical = 6.dp),
    ) {
        Text(
            text = typeName,
            style = WodTheme.typography.titleMedium,
            color = typeColor,
        )
    }
}

@Composable
private fun StatsSummary(
    durationText: String,
    dateText: String,
    colors: WodColors,
    typography: com.wod.app.ui.theme.WodTypography,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = durationText,
            style = typography.headlineLarge.copy(fontSize = 48.sp),
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        if (dateText.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = dateText,
                style = typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun NotesField(value: String, colors: WodColors, onNotes: (String) -> Unit) {
    val typography = WodTheme.typography
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Note",
            style = typography.titleMedium,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onNotes,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Aggiungi una nota sull'allenamento…", color = colors.textDisabled)
            },
            minLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                focusedBorderColor = colors.accentTabata,
                unfocusedBorderColor = colors.divider,
                cursorColor = colors.accentTabata,
            ),
        )
    }
}

@Composable
private fun ActionButtons(
    colors: WodColors,
    typography: com.wod.app.ui.theme.WodTypography,
    onShare: () -> Unit,
    onSave: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = onShare,
            modifier = Modifier.weight(1f),
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.divider),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary),
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Condividi", style = typography.bodyLarge)
        }
        Button(
            onClick = onSave,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.success,
                contentColor = colors.bgPrimary,
            ),
        ) {
            Text("Salva", style = typography.bodyLarge)
        }
    }
}

private fun shareWorkout(context: android.content.Context, text: String) {
    val intent = Intent.createChooser(
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        },
        "Condividi allenamento",
    )
    context.startActivity(intent)
}

private fun formatDuration(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

private fun formatDate(ms: Long): String =
    SimpleDateFormat("d MMM yyyy  HH:mm", Locale.getDefault()).format(Date(ms))

private fun TimerType.displayName(): String = when (this) {
    TimerType.AMRAP    -> "AMRAP"
    TimerType.FOR_TIME -> "FOR TIME"
    TimerType.EMOM     -> "EMOM"
    TimerType.TABATA   -> "TABATA"
    TimerType.MIX      -> "MIX"
    TimerType.CUSTOM   -> "CUSTOM"
}

private fun TimerType.accentColor(colors: WodColors) = when (this) {
    TimerType.AMRAP    -> colors.accentAmrap
    TimerType.FOR_TIME -> colors.accentForTime
    TimerType.EMOM     -> colors.accentEmom
    TimerType.TABATA   -> colors.accentTabata
    TimerType.MIX      -> colors.accentMix
    TimerType.CUSTOM   -> colors.accentCustom
}

// T04d / T56b — 4-variant previews

@WodPreview
@androidx.compose.runtime.Composable
private fun CompletionScreenPreview() {
    WodTheme { CompletionScreen(onDone = {}) }
}


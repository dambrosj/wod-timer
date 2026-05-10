package com.wod.app.ui.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.wod.app.R
import com.wod.app.WodApp
import com.wod.app.domain.engine.TabataEngine
import com.wod.app.domain.model.TimerConfig
import com.wod.app.domain.model.WodRepeatConfig
import com.wod.app.ui.components.ExercisesBlock
import com.wod.app.ui.components.NumberPicker
import com.wod.app.ui.components.TimePicker
import com.wod.app.ui.components.WodRepeatBlock
import com.wod.app.ui.theme.WodPreview
import com.wod.app.ui.theme.WodTheme
import kotlinx.coroutines.launch

/**
 * TABATA timer configuration screen (T22).
 *
 * Fields: Series / Lavoro / Riposo / WodRepeatBlock / notes / CTA.
 * Defaults: 9 series, 20s work, 10s rest.
 */
@Composable
fun TabataConfigScreen(
    onBack: () -> Unit,
    onStart: (TimerConfig.Tabata) -> Unit,
    initialConfig: TimerConfig.Tabata? = null,
    onSaveEdited: ((TimerConfig.Tabata) -> Unit)? = null,
) {
    var series by rememberSaveable { mutableIntStateOf(initialConfig?.series ?: 9) }
    var workSeconds by rememberSaveable { mutableIntStateOf(initialConfig?.workSeconds ?: 20) }
    var restSeconds by rememberSaveable { mutableIntStateOf(initialConfig?.restSeconds ?: 10) }
    var repeat by rememberSaveable(stateSaver = WodRepeatConfigSaver) { mutableStateOf(initialConfig?.repeat ?: WodRepeatConfig()) }
    var exercises by rememberSaveable(stateSaver = StringListSaver) { mutableStateOf(initialConfig?.exercises ?: emptyList()) }
    var notes by rememberSaveable { mutableStateOf(initialConfig?.notes ?: "") }

    val config = TimerConfig.Tabata(
        series = series,
        workSeconds = workSeconds,
        restSeconds = restSeconds,
        exercises = exercises,
        repeat = repeat,
        notes = notes,
    )

    val scope = rememberCoroutineScope()
    val wodApp = LocalContext.current.applicationContext as WodApp

    ConfigScaffold(
        title = "TABATA",
        totalSeconds = TabataEngine.totalSeconds(config),
        onBack = onBack,
        onStart = { if (onSaveEdited != null) onSaveEdited(config) else onStart(config) },
        ctaLabel = if (onSaveEdited != null) "SALVA MODIFICHE" else null,
        showBookmark = onSaveEdited == null,
        onSaveAsWod = if (onSaveEdited == null) { name, desc ->
            scope.launch { wodApp.savedWodRepository.saveFromConfig(name, desc, config) }
        } else null,
    ) {
        Spacer(Modifier.height(WodTheme.spacing.l))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(WodTheme.spacing.m),
        ) {
            NumberPicker(
                value = series,
                onValueChange = { series = it },
                label = stringResource(R.string.config_series),
                min = 1, max = 99,
            )
            TimePicker(
                totalSeconds = workSeconds,
                onValueChange = { workSeconds = it },
                label = stringResource(R.string.config_work),
                maxMinutes = 59,
            )
            TimePicker(
                totalSeconds = restSeconds,
                onValueChange = { restSeconds = it },
                label = stringResource(R.string.config_rest),
                maxMinutes = 59,
            )
        }

        Spacer(Modifier.height(WodTheme.spacing.l))

        ExercisesBlock(
            exercises = exercises,
            seriesCount = series,
            onExercisesChange = { exercises = it },
        )

        Spacer(Modifier.height(WodTheme.spacing.l))

        WodRepeatBlock(
            config = repeat,
            onConfigChange = { repeat = it },
            modifier = Modifier,
        )

        Spacer(Modifier.height(WodTheme.spacing.l))

        NotesField(value = notes, onValueChange = { notes = it })

        Spacer(Modifier.height(WodTheme.spacing.l))
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────

@Composable
internal fun NotesField(value: String, onValueChange: (String) -> Unit) {
    val colors = WodTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                stringResource(R.string.config_add_note),
                style = WodTheme.typography.bodyMedium,
                color = colors.textDisabled,
            )
        },
        modifier = Modifier.fillMaxWidth(),
        maxLines = 4,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        textStyle = WodTheme.typography.bodyMedium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            focusedBorderColor = colors.textSecondary,
            unfocusedBorderColor = colors.divider,
            cursorColor = colors.accentTabata,
        ),
    )
}

// T04d / T56b — 4-variant previews

@WodPreview
@androidx.compose.runtime.Composable
private fun TabataConfigScreenPreview() {
    WodTheme { TabataConfigScreen(onBack = {}, onStart = {}) }
}


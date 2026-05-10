package com.wod.app.ui.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.wod.app.R
import com.wod.app.WodApp
import com.wod.app.domain.engine.ForTimeEngine
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
 * FOR TIME timer configuration screen (T24).
 *
 * Fields: Timecap / Rounds / WodRepeatBlock / notes / CTA.
 * Defaults: 10 minutes timecap, 1 round.
 */
@Composable
fun ForTimeConfigScreen(
    onBack: () -> Unit,
    onStart: (TimerConfig.ForTime) -> Unit,
    initialConfig: TimerConfig.ForTime? = null,
    onSaveEdited: ((TimerConfig.ForTime) -> Unit)? = null,
) {
    var timecapSeconds by rememberSaveable { mutableIntStateOf(initialConfig?.timecapSeconds ?: (10 * 60)) }
    var rounds by rememberSaveable { mutableIntStateOf(initialConfig?.rounds ?: 1) }
    var repeat by rememberSaveable(stateSaver = WodRepeatConfigSaver) { mutableStateOf(initialConfig?.repeat ?: WodRepeatConfig()) }
    var exercises by rememberSaveable(stateSaver = StringListSaver) { mutableStateOf(initialConfig?.exercises ?: emptyList()) }
    var notes by rememberSaveable { mutableStateOf(initialConfig?.notes ?: "") }

    val config = TimerConfig.ForTime(
        timecapSeconds = timecapSeconds,
        rounds = rounds,
        exercises = exercises,
        repeat = repeat,
        notes = notes,
    )

    val scope = rememberCoroutineScope()
    val wodApp = LocalContext.current.applicationContext as WodApp

    ConfigScaffold(
        title = "FOR TIME",
        totalSeconds = ForTimeEngine.totalSeconds(config),
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
            TimePicker(
                totalSeconds = timecapSeconds,
                onValueChange = { timecapSeconds = it },
                label = "Timecap",
            )
            NumberPicker(
                value = rounds,
                onValueChange = { rounds = it },
                label = stringResource(R.string.config_wod_rounds),
                min = 1, max = 20,
            )
        }

        Spacer(Modifier.height(WodTheme.spacing.l))

        ExercisesBlock(
            exercises = exercises,
            seriesCount = rounds.coerceAtLeast(1),
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

// T04d / T56b — 4-variant previews

@WodPreview
@androidx.compose.runtime.Composable
private fun ForTimeConfigScreenPreview() {
    WodTheme { ForTimeConfigScreen(onBack = {}, onStart = {}) }
}

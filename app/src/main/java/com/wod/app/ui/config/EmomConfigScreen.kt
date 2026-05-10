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
import com.wod.app.WodApp
import com.wod.app.domain.engine.EmomEngine
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
 * EMOM timer configuration screen (T25).
 *
 * Fields: Total minutes / Interval / WodRepeatBlock / notes / CTA.
 * Defaults: 10 minutes total, 60s interval.
 */
@Composable
fun EmomConfigScreen(
    onBack: () -> Unit,
    onStart: (TimerConfig.Emom) -> Unit,
    initialConfig: TimerConfig.Emom? = null,
    onSaveEdited: ((TimerConfig.Emom) -> Unit)? = null,
) {
    var totalMinutes by rememberSaveable { mutableIntStateOf(initialConfig?.totalMinutes ?: 10) }
    var intervalSeconds by rememberSaveable { mutableIntStateOf(initialConfig?.intervalSeconds ?: 60) }
    var repeat by rememberSaveable(stateSaver = WodRepeatConfigSaver) { mutableStateOf(initialConfig?.repeat ?: WodRepeatConfig()) }
    var exercises by rememberSaveable(stateSaver = StringListSaver) { mutableStateOf(initialConfig?.exercises ?: emptyList()) }
    var notes by rememberSaveable { mutableStateOf(initialConfig?.notes ?: "") }

    // Number of intervals drives the "per serie" field count (capped at 20 for UX).
    val intervals = ((totalMinutes * 60) / intervalSeconds.coerceAtLeast(1)).coerceIn(1, 20)

    val config = TimerConfig.Emom(
        totalMinutes = totalMinutes,
        intervalSeconds = intervalSeconds,
        exercises = exercises,
        repeat = repeat,
        notes = notes,
    )

    val scope = rememberCoroutineScope()
    val wodApp = LocalContext.current.applicationContext as WodApp

    ConfigScaffold(
        title = "EMOM",
        totalSeconds = EmomEngine.totalSeconds(config),
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
                value = totalMinutes,
                onValueChange = { totalMinutes = it },
                label = "Durata (min)",
                min = 1, max = 120,
            )
            TimePicker(
                totalSeconds = intervalSeconds,
                onValueChange = { intervalSeconds = it.coerceAtLeast(1) },
                label = "Intervallo",
                maxMinutes = 59,
            )
        }

        Spacer(Modifier.height(WodTheme.spacing.l))

        ExercisesBlock(
            exercises = exercises,
            seriesCount = intervals,
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
private fun EmomConfigScreenPreview() {
    WodTheme { EmomConfigScreen(onBack = {}, onStart = {}) }
}

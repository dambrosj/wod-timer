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
import com.wod.app.domain.engine.AmrapEngine
import com.wod.app.domain.model.TimerConfig
import com.wod.app.domain.model.WodRepeatConfig
import com.wod.app.ui.components.ExerciseSequenceBlock
import com.wod.app.ui.components.TimePicker
import com.wod.app.ui.components.WodRepeatBlock
import com.wod.app.ui.theme.WodPreview
import com.wod.app.ui.theme.WodTheme
import kotlinx.coroutines.launch

/**
 * AMRAP timer configuration screen (T23).
 *
 * Fields: Durata / WodRepeatBlock / notes / CTA.
 * Default: 8 minutes duration.
 */
@Composable
fun AmrapConfigScreen(
    onBack: () -> Unit,
    onStart: (TimerConfig.Amrap) -> Unit,
    initialConfig: TimerConfig.Amrap? = null,
    onSaveEdited: ((TimerConfig.Amrap) -> Unit)? = null,
    initialWodName: String? = null,
    initialWodDesc: String? = null,
    onRenameWod: ((String, String) -> Unit)? = null,
) {
    var durationSeconds by rememberSaveable { mutableIntStateOf(initialConfig?.durationSeconds ?: (8 * 60)) }
    var repeat by rememberSaveable(stateSaver = WodRepeatConfigSaver) { mutableStateOf(initialConfig?.repeat ?: WodRepeatConfig()) }
    var exercises by rememberSaveable(stateSaver = StringListSaver) { mutableStateOf(initialConfig?.exercises ?: emptyList()) }
    var exerciseReps by rememberSaveable(stateSaver = IntListSaver) { mutableStateOf(initialConfig?.exerciseReps ?: emptyList()) }
    var exerciseMinReps by rememberSaveable(stateSaver = IntListSaver) { mutableStateOf(initialConfig?.exerciseMinReps ?: emptyList()) }
    var notes by rememberSaveable { mutableStateOf(initialConfig?.notes ?: "") }

    val config = TimerConfig.Amrap(
        durationSeconds = durationSeconds,
        exercises = exercises,
        exerciseReps = exerciseReps,
        exerciseMinReps = exerciseMinReps,
        repeat = repeat,
        notes = notes,
    )

    val scope = rememberCoroutineScope()
    val wodApp = LocalContext.current.applicationContext as WodApp

    ConfigScaffold(
        title = "AMRAP",
        totalSeconds = AmrapEngine.totalSeconds(config),
        onBack = onBack,
        onStart = { if (onSaveEdited != null) onSaveEdited(config) else onStart(config) },
        ctaLabel = if (onSaveEdited != null) "SALVA MODIFICHE" else null,
        showBookmark = onSaveEdited == null,
        onSaveAsWod = if (onSaveEdited == null) { name, desc ->
            scope.launch { wodApp.savedWodRepository.saveFromConfig(name, desc, config) }
        } else null,
        initialWodName = initialWodName,
        initialWodDesc = initialWodDesc,
        onRenameWod = onRenameWod,
    ) {
        Spacer(Modifier.height(WodTheme.spacing.l))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(WodTheme.spacing.m),
        ) {
            TimePicker(
                totalSeconds = durationSeconds,
                onValueChange = { durationSeconds = it },
                label = "Durata",
            )
        }

        Spacer(Modifier.height(WodTheme.spacing.l))

        ExerciseSequenceBlock(
            exercises = exercises,
            exerciseReps = exerciseReps,
            exerciseMinReps = exerciseMinReps,
            onExercisesChange = { exercises = it },
            onRepsChange = { exerciseReps = it },
            onMinRepsChange = { exerciseMinReps = it },
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
private fun AmrapConfigScreenPreview() {
    WodTheme { AmrapConfigScreen(onBack = {}, onStart = {}) }
}

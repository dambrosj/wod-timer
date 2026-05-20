package com.wod.app.ui.config

import androidx.compose.runtime.Composable
import com.wod.app.domain.model.TimerConfig
import com.wod.app.domain.model.TimerType
import com.wod.app.ui.components.PlaceholderScreen

/**
 * Entry-point for the config route. Dispatches to the per-type config screen.
 *
 * [onStart] receives the fully-built [TimerConfig] so the caller (nav graph)
 * can forward it to the engine / foreground service.
 * [initialConfig] and [onSaveEdited] are used when editing an existing WOD.
 */
@Composable
fun ConfigScreen(
    type: TimerType,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onStartWithConfig: ((TimerConfig) -> Unit)? = null,
    initialConfig: TimerConfig? = null,
    onSaveEdited: ((TimerConfig) -> Unit)? = null,
    initialWodName: String? = null,
    initialWodDesc: String? = null,
    onRenameWod: ((String, String) -> Unit)? = null,
) {
    val start: (TimerConfig) -> Unit = { cfg -> onStartWithConfig?.invoke(cfg) ?: onStart() }

    when (type) {
        TimerType.TABATA   -> TabataConfigScreen(
            onBack = onBack,
            onStart = { start(it) },
            initialConfig = initialConfig as? TimerConfig.Tabata,
            onSaveEdited = if (onSaveEdited != null) { cfg -> onSaveEdited(cfg) } else null,
            initialWodName = initialWodName,
            initialWodDesc = initialWodDesc,
            onRenameWod = onRenameWod,
        )
        TimerType.AMRAP    -> AmrapConfigScreen(
            onBack = onBack,
            onStart = { start(it) },
            initialConfig = initialConfig as? TimerConfig.Amrap,
            onSaveEdited = if (onSaveEdited != null) { cfg -> onSaveEdited(cfg) } else null,
            initialWodName = initialWodName,
            initialWodDesc = initialWodDesc,
            onRenameWod = onRenameWod,
        )
        TimerType.FOR_TIME -> ForTimeConfigScreen(
            onBack = onBack,
            onStart = { start(it) },
            initialConfig = initialConfig as? TimerConfig.ForTime,
            onSaveEdited = if (onSaveEdited != null) { cfg -> onSaveEdited(cfg) } else null,
            initialWodName = initialWodName,
            initialWodDesc = initialWodDesc,
            onRenameWod = onRenameWod,
        )
        TimerType.EMOM     -> EmomConfigScreen(
            onBack = onBack,
            onStart = { start(it) },
            initialConfig = initialConfig as? TimerConfig.Emom,
            onSaveEdited = if (onSaveEdited != null) { cfg -> onSaveEdited(cfg) } else null,
            initialWodName = initialWodName,
            initialWodDesc = initialWodDesc,
            onRenameWod = onRenameWod,
        )
        TimerType.MIX      -> PlaceholderScreen(
            title = "MIX",
            onBack = onBack,
            body = "MIX (Premium) — configura i segmenti (T27).",
        )
        TimerType.CUSTOM   -> CustomConfigScreen(
            onBack = onBack,
            onStart = { start(it) },
            initialConfig = initialConfig as? TimerConfig.Custom,
            onSaveEdited = if (onSaveEdited != null) { cfg -> onSaveEdited(cfg) } else null,
            initialWodName = initialWodName,
            initialWodDesc = initialWodDesc,
            onRenameWod = onRenameWod,
        )
    }
}


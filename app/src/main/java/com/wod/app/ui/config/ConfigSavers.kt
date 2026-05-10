package com.wod.app.ui.config

import androidx.compose.runtime.saveable.Saver
import com.wod.app.domain.model.WodRepeatConfig
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Allows [WodRepeatConfig] to survive Activity recreation / process death
 * inside `rememberSaveable { mutableStateOf(...) }`.
 */
val WodRepeatConfigSaver: Saver<WodRepeatConfig, String> = Saver(
    save    = { Json.encodeToString(it) },
    restore = { Json.decodeFromString(it) },
)

/**
 * Allows `List<String>` (exercise names, tags…) to survive Activity recreation.
 *
 * Usage:
 * ```kotlin
 * var exercises by rememberSaveable(stateSaver = StringListSaver) { mutableStateOf(emptyList()) }
 * ```
 */
val StringListSaver: Saver<List<String>, String> = Saver(
    save    = { Json.encodeToString(ListSerializer(String.serializer()), it) },
    restore = { Json.decodeFromString(ListSerializer(String.serializer()), it) },
)

package com.wod.app.data.repository

import com.wod.app.domain.model.SavedWod
import com.wod.app.domain.model.TimerType
import kotlinx.serialization.Serializable

/**
 * JSON-serializable DTO for WOD export/import.
 * Timestamps are in Unix **seconds** (Double) to match the iOS format,
 * so files can be shared between platforms.
 */
@Serializable
data class SavedWodExportDto(
    val id: String,
    val name: String,
    val type: String,
    val configJson: String,
    val description: String = "",
    val tags: List<String> = emptyList(),
    val createdAt: Double,
    val lastUsedAt: Double? = null,
    val timesUsed: Int = 0,
    val isFavourite: Boolean = false,
    val isBuiltIn: Boolean = false,
)

fun SavedWod.toExportDto() = SavedWodExportDto(
    id = id,
    name = name,
    type = type.name,
    configJson = configJson,
    description = description,
    tags = tags,
    createdAt = createdAt / 1000.0,
    lastUsedAt = lastUsedAt?.let { it / 1000.0 },
    timesUsed = timesUsed,
    isFavourite = isFavourite,
    isBuiltIn = isBuiltIn,
)

fun SavedWodExportDto.toDomain(): SavedWod? = runCatching {
    SavedWod(
        id = id,
        name = name,
        type = TimerType.valueOf(type),
        configJson = configJson,
        description = description,
        tags = tags,
        createdAt = (createdAt * 1000).toLong(),
        lastUsedAt = lastUsedAt?.let { (it * 1000).toLong() },
        timesUsed = timesUsed,
        isFavourite = isFavourite,
        isBuiltIn = isBuiltIn,
    )
}.getOrNull()

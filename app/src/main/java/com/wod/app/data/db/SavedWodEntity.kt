package com.wod.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wod.app.domain.model.SavedWod
import com.wod.app.domain.model.TimerType
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val tagJson = Json { ignoreUnknownKeys = true }
private val stringListSerializer = ListSerializer(String.serializer())

@Entity(tableName = "saved_wods")
data class SavedWodEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val configJson: String,
    val description: String,
    /** JSON array of tag strings. */
    val tagsJson: String,
    val createdAt: Long,
    val lastUsedAt: Long?,
    val timesUsed: Int,
    val isFavourite: Boolean,
    val isBuiltIn: Boolean,
)

fun SavedWodEntity.toDomain() = SavedWod(
    id = id,
    name = name,
    type = TimerType.valueOf(type),
    configJson = configJson,
    description = description,
    tags = runCatching { tagJson.decodeFromString(stringListSerializer, tagsJson) }
        .getOrDefault(emptyList()),
    createdAt = createdAt,
    lastUsedAt = lastUsedAt,
    timesUsed = timesUsed,
    isFavourite = isFavourite,
    isBuiltIn = isBuiltIn,
)

fun SavedWod.toEntity() = SavedWodEntity(
    id = id,
    name = name,
    type = type.name,
    configJson = configJson,
    description = description,
    tagsJson = tagJson.encodeToString(stringListSerializer, tags),
    createdAt = createdAt,
    lastUsedAt = lastUsedAt,
    timesUsed = timesUsed,
    isFavourite = isFavourite,
    isBuiltIn = isBuiltIn,
)

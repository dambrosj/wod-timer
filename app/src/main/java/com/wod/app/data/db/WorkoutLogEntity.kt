package com.wod.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wod.app.domain.model.TimerType
import com.wod.app.domain.model.WorkoutLog

@Entity(tableName = "workout_logs")
data class WorkoutLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val configJson: String,
    val durationSeconds: Int,
    val completedAt: Long,
    val notes: String,
    val savedWodId: String?,
)

fun WorkoutLogEntity.toDomain() = WorkoutLog(
    id = id,
    type = TimerType.valueOf(type),
    configJson = configJson,
    durationSeconds = durationSeconds,
    completedAt = completedAt,
    notes = notes,
    savedWodId = savedWodId,
)

fun WorkoutLog.toEntity() = WorkoutLogEntity(
    id = id,
    type = type.name,
    configJson = configJson,
    durationSeconds = durationSeconds,
    completedAt = completedAt,
    notes = notes,
    savedWodId = savedWodId,
)

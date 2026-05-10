package com.wod.app.data.repository

import com.wod.app.data.db.WorkoutLogDao
import com.wod.app.data.db.toDomain
import com.wod.app.data.db.toEntity
import com.wod.app.domain.model.TimerType
import com.wod.app.domain.model.WorkoutLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkoutRepository(private val dao: WorkoutLogDao) {

    val historyFlow: Flow<List<WorkoutLog>> = dao.getAllFlow().map { it.map { e -> e.toDomain() } }

    fun byTypeFlow(type: TimerType): Flow<List<WorkoutLog>> =
        dao.getByTypeFlow(type.name).map { it.map { e -> e.toDomain() } }

    suspend fun log(workoutLog: WorkoutLog): Long = dao.insert(workoutLog.toEntity())

    suspend fun getById(id: Long): WorkoutLog? = dao.getById(id)?.toDomain()

    suspend fun updateNotes(id: Long, notes: String) = dao.updateNotes(id, notes)

    suspend fun delete(id: Long) = dao.deleteById(id)

    suspend fun clearHistory() = dao.deleteAll()
}

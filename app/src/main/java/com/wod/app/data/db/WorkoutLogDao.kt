package com.wod.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutLogDao {

    @Query("SELECT * FROM workout_logs ORDER BY completedAt DESC")
    fun getAllFlow(): Flow<List<WorkoutLogEntity>>

    @Query("SELECT * FROM workout_logs WHERE type = :type ORDER BY completedAt DESC")
    fun getByTypeFlow(type: String): Flow<List<WorkoutLogEntity>>

    @Insert
    suspend fun insert(entity: WorkoutLogEntity): Long

    @Query("SELECT * FROM workout_logs WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): WorkoutLogEntity?

    @Query("UPDATE workout_logs SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String)

    @Query("DELETE FROM workout_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM workout_logs")
    suspend fun deleteAll()
}

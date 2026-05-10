package com.wod.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedWodDao {

    @Query("SELECT * FROM saved_wods ORDER BY isFavourite DESC, name ASC")
    fun getAllFlow(): Flow<List<SavedWodEntity>>

    @Query("SELECT * FROM saved_wods WHERE isFavourite = 1 ORDER BY name ASC")
    fun getFavouritesFlow(): Flow<List<SavedWodEntity>>

    @Query("SELECT * FROM saved_wods WHERE type = :type ORDER BY isFavourite DESC, name ASC")
    fun getByTypeFlow(type: String): Flow<List<SavedWodEntity>>

    @Query("SELECT * FROM saved_wods WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): SavedWodEntity?

    /** Silently ignores duplicate IDs — used for seeding built-in WODs. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(entity: SavedWodEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SavedWodEntity)

    @Update
    suspend fun update(entity: SavedWodEntity)

    @Query("DELETE FROM saved_wods WHERE id = :id")
    suspend fun deleteById(id: String)
}

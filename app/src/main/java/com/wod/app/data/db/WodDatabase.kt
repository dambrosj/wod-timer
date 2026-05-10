package com.wod.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SavedWodEntity::class, WorkoutLogEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class WodDatabase : RoomDatabase() {

    abstract fun savedWodDao(): SavedWodDao
    abstract fun workoutLogDao(): WorkoutLogDao

    companion object {
        fun build(context: Context): WodDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                WodDatabase::class.java,
                "wod.db",
            )
                .fallbackToDestructiveMigration()
                .build()
    }
}

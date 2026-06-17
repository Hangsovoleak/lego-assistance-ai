package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        LegoBrickEntity::class,
        CustomCreationEntity::class,
        SavedBuildEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LegoDatabase : RoomDatabase() {
    abstract val legoDao: LegoDao

    companion object {
        @Volatile
        private var INSTANCE: LegoDatabase? = null

        fun getInstance(context: Context): LegoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LegoDatabase::class.java,
                    "lego_assistant_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

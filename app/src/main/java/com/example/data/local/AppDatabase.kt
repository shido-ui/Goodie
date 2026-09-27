package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.*
import com.example.data.local.entities.*

@Database(
    entities = [
        SessionEntity::class,
        WorldStateEntity::class,
        ChatMessageEntity::class,
        SimulationLogDbEntity::class,
        LoreDbEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao
    abstract fun worldStateDao(): WorldStateDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun simulationLogDao(): SimulationLogDao
    abstract fun loreDao(): LoreDao

    companion object {
        private const val DB_NAME = "rpg_ai_hub_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

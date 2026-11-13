package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SchoolClassEntity::class,
        HomeworkEntity::class,
        StudyTopicEntity::class,
        BagItemEntity::class,
        ExamEntity::class,
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HubDatabase : RoomDatabase() {
    abstract fun hubDao(): HubDao

    companion object {
        @Volatile
        private var INSTANCE: HubDatabase? = null

        fun getInstance(context: Context): HubDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HubDatabase::class.java,
                    "my_hub.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

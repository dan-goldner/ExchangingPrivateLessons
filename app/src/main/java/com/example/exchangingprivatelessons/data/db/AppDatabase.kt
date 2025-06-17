package com.example.exchangingprivatelessons.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.exchangingprivatelessons.data.dao.LessonDao
import com.example.exchangingprivatelessons.data.entity.LessonEntity

@Database(entities = [LessonEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonDao(): LessonDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lessons.db"
                ).build().also { INSTANCE = it }
            }
    }
}

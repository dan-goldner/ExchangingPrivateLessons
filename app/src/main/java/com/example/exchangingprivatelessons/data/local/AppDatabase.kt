package com.example.exchangingprivatelessons.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.exchangingprivatelessons.common.util.TimestampConverter
import com.example.exchangingprivatelessons.data.local.dao.*
import com.example.exchangingprivatelessons.data.local.entity.*

@Database(
    entities = [
        ChatEntity::class,
        LessonEntity::class,
        LessonRequestEntity::class,
        MessageEntity::class,
        RatingEntity::class,
        TakenLessonEntity::class,
        UserEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(TimestampConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao
    abstract fun lessonDao(): LessonDao
    abstract fun lessonRequestDao(): LessonRequestDao
    abstract fun messageDao(): MessageDao
    abstract fun ratingDao(): RatingDao
    abstract fun takenLessonDao(): TakenLessonDao
    abstract fun userDao(): UserDao
}

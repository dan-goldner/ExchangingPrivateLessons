package com.example.exchangingprivatelessons.data.local

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 5,
    exportSchema = false
)
@TypeConverters(TimestampConverter::class)
abstract class AppDatabase : RoomDatabase() {

    /* ---------- DAO‑ים ---------- */
    abstract fun chatDao()            : ChatDao
    abstract fun lessonDao()          : LessonDao
    abstract fun lessonRequestDao()   : LessonRequestDao
    abstract fun messageDao()         : MessageDao
    abstract fun ratingDao()          : RatingDao
    abstract fun takenLessonDao()     : TakenLessonDao
    abstract fun userDao()            : UserDao

    /* ---------- מיגרציות ---------- */
    companion object {

        /** 2 → 3 – הופך תאריכים ל‑NOT NULL DEFAULT 0 */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    CREATE TABLE users_new (
                        id           TEXT PRIMARY KEY NOT NULL,
                        displayName  TEXT NOT NULL,
                        email        TEXT NOT NULL,
                        photoUrl     TEXT NOT NULL,
                        bio          TEXT NOT NULL,
                        score        INTEGER NOT NULL,
                        createdAt    INTEGER NOT NULL DEFAULT 0,
                        lastLoginAt  INTEGER NOT NULL DEFAULT 0,
                        lastUpdated  INTEGER
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    INSERT INTO users_new
                    SELECT id,
                           displayName,
                           email,
                           photoUrl,
                           bio,
                           score,
                           IFNULL(createdAt ,0),
                           IFNULL(lastLoginAt,0),
                           lastUpdated
                    FROM users
                    """.trimIndent()
                )

                db.execSQL("DROP TABLE users")
                db.execSQL("ALTER TABLE users_new RENAME TO users")
            }
        }

        /** 3 → 4 – מחזיר את createdAt / lastLoginAt ל‑NULL‑able  */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    CREATE TABLE users_new (
                        id           TEXT PRIMARY KEY NOT NULL,
                        displayName  TEXT NOT NULL,
                        email        TEXT NOT NULL,
                        photoUrl     TEXT NOT NULL,
                        bio          TEXT NOT NULL,
                        score        INTEGER NOT NULL,
                        createdAt    INTEGER,
                        lastLoginAt  INTEGER,
                        lastUpdated  INTEGER
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    INSERT INTO users_new
                    SELECT id,
                           displayName,
                           email,
                           photoUrl,
                           bio,
                           score,
                           NULLIF(createdAt ,0),
                           NULLIF(lastLoginAt,0),
                           lastUpdated
                    FROM users
                    """.trimIndent()
                )

                db.execSQL("DROP TABLE users")
                db.execSQL("ALTER TABLE users_new RENAME TO users")
            }
        }
    }
}

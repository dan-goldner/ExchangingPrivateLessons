package com.example.exchangingprivatelessons.data.local.dao

import android.util.Log
import androidx.room.*
import com.example.exchangingprivatelessons.data.local.entity.LessonEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach



@Dao
interface LessonDao {

    @Query("SELECT * FROM lessons WHERE status = :status ORDER BY CASE WHEN createdAt IS NULL THEN 1 ELSE 0 END, createdAt DESC")
    fun observeByStatus(status: String): Flow<List<LessonEntity>>



    @Query("""
        SELECT * FROM lessons
        WHERE status = 'Active'
        ORDER BY
            CASE WHEN createdAt IS NULL THEN 1 ELSE 0 END,
            createdAt DESC
    """)
    fun observeActive(): Flow<List<LessonEntity>>

    //  (extension for debug)
    fun LessonDao.observeActiveDebug(tag: String) =
        observeActive().onEach { list ->
            Log.d(tag, "Room emitted ${list.size} rows:")
            list.forEach { Log.d(tag, "  ${it.id} owner=${it.ownerId} status=${it.status}") }
        }


    /* 🔄 לכל השיעורים שלי – בלי סינון Status */
    @Query("""
        SELECT * FROM lessons
        WHERE ownerId = :uid
        ORDER BY
            CASE WHEN createdAt IS NULL THEN 1 ELSE 0 END,
            createdAt DESC
    """)
    fun observeMine(uid: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :id")
    fun observe(id: String): Flow<LessonEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(lesson: LessonEntity)

    @Query("UPDATE lessons SET status = :status WHERE id = :id")
    suspend fun setStatus(id: String, status: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(lessons: List<LessonEntity>)

    @Query("DELETE FROM lessons WHERE id = :lessonId")
    suspend fun delete(lessonId: String)

    @Query("SELECT * FROM lessons WHERE id = :id LIMIT 1")
    suspend fun get(id: String): LessonEntity?

    /*@Query("""
    SELECT * FROM lessons WHERE id IN (:lessonIds)
    ORDER BY createdAt DESC
""")
    fun observeLessonsByIds(lessonIds: List<String>): Flow<List<LessonEntity>>*/
    @Query("SELECT * FROM lessons WHERE id IN (:ids)")
    fun observeLessonsByIds(ids: List<String>): Flow<List<LessonEntity>>


    @Query("DELETE FROM lessons WHERE id NOT IN (:ids)")
    suspend fun deleteAllExcept(ids: List<String>)


}

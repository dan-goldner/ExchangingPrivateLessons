package com.example.exchangingprivatelessons.data.local.dao

import androidx.room.*
import com.example.exchangingprivatelessons.data.local.entity.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {

    @Query("SELECT * FROM lessons WHERE status = :status ORDER BY CASE WHEN createdAt IS NULL THEN 1 ELSE 0 END, createdAt DESC")
    fun observeByStatus(status: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE status = 'Active' ORDER BY CASE WHEN createdAt IS NULL THEN 1 ELSE 0 END, createdAt DESC")
    fun observeActive(): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE status = 'Active' AND ownerId = :uid ORDER BY CASE WHEN createdAt IS NULL THEN 1 ELSE 0 END, createdAt DESC")
    fun observeMine(uid: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :id")
    fun observe(id: String): Flow<LessonEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(lesson: LessonEntity)

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

}

package com.example.exchangingprivatelessons.data.local.dao

import androidx.room.*
import com.example.exchangingprivatelessons.data.local.entity.LessonRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonRequestDao {

    @Query("""
    SELECT * FROM lesson_requests
    WHERE (ownerId = :uid OR requesterId = :uid)
    AND status = :status
    ORDER BY CASE WHEN requestedAt IS NULL THEN 1 ELSE 0 END, requestedAt DESC""")
    fun observeByStatus(uid: String, status: String): Flow<List<LessonRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(request: LessonRequestEntity)

    @Query("DELETE FROM lesson_requests WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM lesson_requests ORDER BY requestedAt DESC")
    fun observeAll(): Flow<List<LessonRequestEntity>>

    @Query("SELECT * FROM lesson_requests WHERE ownerId = :ownerUid ORDER BY requestedAt DESC")
    fun observeIncoming(ownerUid: String): Flow<List<LessonRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(requests: List<LessonRequestEntity>)

    @Query("""
    SELECT * FROM lesson_requests
    WHERE lessonId = :lessonId AND requesterId = :uid
    LIMIT 1
""")
    suspend fun getMyRequest(lessonId: String, uid: String): LessonRequestEntity?


}

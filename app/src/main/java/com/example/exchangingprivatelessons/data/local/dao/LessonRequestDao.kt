package com.example.exchangingprivatelessons.data.local.dao

import androidx.room.*
import com.example.exchangingprivatelessons.data.local.entity.LessonRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonRequestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(request: LessonRequestEntity)

    @Query("DELETE FROM lesson_requests WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM lesson_requests ORDER BY requestedAt DESC")
    fun observeAll(): Flow<List<LessonRequestEntity>>


    @Query("DELETE FROM lesson_requests WHERE id NOT IN (:ids)")
    suspend fun deleteMissing(ids: List<String>)

    @Query("""
    SELECT * FROM lesson_requests
    WHERE lessonId = :lessonId AND requesterId = :uid
    LIMIT 1
""")
    suspend fun getMyRequest(lessonId: String, uid: String): LessonRequestEntity?

    @Query("""
        SELECT lessonId FROM lesson_requests
        WHERE requesterId = :userId AND status = 'approved'
    """)
    fun observeTakenByUser(userId: String): Flow<List<String>>

    @Query("""
        SELECT * FROM lesson_requests
        WHERE ownerId = :ownerUid         -- בקשות אליי
        ORDER BY requestedAt DESC
    """)
    fun observeIncoming(ownerUid: String): Flow<List<LessonRequestEntity>>

    @Query("""
    SELECT * FROM lesson_requests
    WHERE requesterId = :uid
      AND (:status IS NULL OR status = :status)
    ORDER BY requestedAt DESC
""")
    fun observeByStatus(
        uid: String,
        status: String?            // ← String?  (nullable)
    ): Flow<List<LessonRequestEntity>>



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(list: List<LessonRequestEntity>)

    /* ----- ניקוי רשומות שאינן קיימות יותר בענן ----- */
    @Query("DELETE FROM lesson_requests WHERE requesterId = :uid  AND id NOT IN (:ids)")
    suspend fun deleteMissingSent(uid: String, ids: List<String>)

    @Query("DELETE FROM lesson_requests WHERE ownerId     = :uid  AND id NOT IN (:ids)")
    suspend fun deleteMissingIncoming(uid: String, ids: List<String>)


}
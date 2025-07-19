package com.example.exchangingprivatelessons.data.local.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.Companion.REPLACE
import com.example.exchangingprivatelessons.data.local.entity.RatingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RatingDao {

    @Query("SELECT * FROM ratings WHERE lessonId = :lessonId")
    fun observeForLesson(lessonId: String): Flow<List<RatingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rating: RatingEntity)

    @Query("DELETE FROM ratings WHERE lessonId = :lessonId AND userId = :userId")
    suspend fun delete(lessonId: String, userId: String)

    @Query("SELECT * FROM ratings")
    fun observeAll(): Flow<List<RatingEntity>>

    @Insert(onConflict = REPLACE)
    suspend fun upsertAll(list: List<RatingEntity>)

    @Query("""
        SELECT * FROM ratings
        WHERE lessonId = :lessonId AND userId = :userId
        LIMIT 1
    """)
    suspend fun getMyRating(
        lessonId: String,
        userId: String
    ): RatingEntity?

}

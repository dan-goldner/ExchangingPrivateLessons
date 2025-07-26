package com.example.exchangingprivatelessons.data.local.dao

import androidx.room.*
import com.example.exchangingprivatelessons.data.local.entity.TakenLessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TakenLessonDao {
    @Query("SELECT * FROM taken_lessons ORDER BY takenAt DESC")
    fun observeAll(): Flow<List<TakenLessonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(list: List<TakenLessonEntity>)

    @Query("DELETE FROM taken_lessons")
    suspend fun clearAll()

}
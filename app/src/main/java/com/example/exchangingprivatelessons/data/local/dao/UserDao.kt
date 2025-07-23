package com.example.exchangingprivatelessons.data.local.dao

import androidx.room.*
import com.example.exchangingprivatelessons.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    /* --- זרמים --- */
    @Query("SELECT * FROM users WHERE id = :id")
    fun observe(id: String): Flow<UserEntity?>

    /* --- upsert יחיד + רשימה --- */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(list: List<UserEntity>)

    /* --- Get חד‑פעמי --- */
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun get(id: String): UserEntity?

    /* --- החלפה מלאה --- */
    @Query("DELETE FROM users")
    suspend fun clear(): Int

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteById(id: String): Int


    @Transaction
    suspend fun replaceAll(list: List<UserEntity>) {
        clear()
        upsertAll(list)
    }
}

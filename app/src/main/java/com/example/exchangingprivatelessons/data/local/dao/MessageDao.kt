package com.example.exchangingprivatelessons.data.local.dao

import androidx.room.*
import com.example.exchangingprivatelessons.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("""
        SELECT * FROM messages
        WHERE chatId = :chatId
        ORDER BY sentAt ASC
    """)
    fun observeForChat(chatId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(messages: List<MessageEntity>)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun clearChat(chatId: String)
}

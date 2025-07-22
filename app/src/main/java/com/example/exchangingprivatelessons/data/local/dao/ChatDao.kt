package com.example.exchangingprivatelessons.data.local.dao

import androidx.room.*
import com.example.exchangingprivatelessons.data.local.entity.ChatEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ChatDao {

    @Query("SELECT * FROM chats ORDER BY CASE WHEN lastMessageAt IS NULL THEN 1 ELSE 0 END, lastMessageAt DESC")
    fun observeAll(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE id = :chatId")
    fun observe(chatId: String): Flow<ChatEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(chat: ChatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(chats: List<ChatEntity>)

    @Query("UPDATE chats SET lastMessage = :text, lastMessageAt = :ts WHERE id = :chatId")
    suspend fun updateLastMessage(chatId: String, text: String, ts: Date?)

    @Query("DELETE FROM chats")            // ← חדש
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(list: List<ChatEntity>) {
        clear()
        upsertAll(list)
    }
}

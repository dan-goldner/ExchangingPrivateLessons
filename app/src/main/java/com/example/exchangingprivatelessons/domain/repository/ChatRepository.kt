package com.example.exchangingprivatelessons.domain.repository

import com.example.exchangingprivatelessons.domain.model.Chat
import kotlinx.coroutines.flow.Flow
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.model.Message

interface ChatRepository {

    /** Live list of all chats a user is in (updates in real-time). */
    fun observeChats(): Flow<Result<List<Chat>>>

    /** Stream a single chat (messages + metadata). */
    fun observeChat(chatId: String): Flow<Result<Chat>>

    /** Send a text message into a chat – server fills sender & timestamp. */
    suspend fun sendMessage(chatId: String, text: String): Result<Unit>

    /** Create (or fetch existing) 1-on-1 chat, returns its id. */
    suspend fun createChat(peerUid: String): Result<String>

    suspend fun forceRefreshChats(): Result<Unit>

    fun observeChatMessages(chatId: String): Flow<Result<List<Message>>>

}

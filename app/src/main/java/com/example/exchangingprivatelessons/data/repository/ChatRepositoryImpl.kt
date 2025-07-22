/*  data/repository/ChatRepositoryImpl.kt  */
package com.example.exchangingprivatelessons.data.repository

import com.example.exchangingprivatelessons.common.di.IoDispatcher
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.common.util.mapList
import com.example.exchangingprivatelessons.data.local.dao.ChatDao
import com.example.exchangingprivatelessons.data.local.dao.UserDao        // ← NEW
import com.example.exchangingprivatelessons.data.local.entity.ChatEntity
import com.example.exchangingprivatelessons.data.mapper.ChatMapper
import com.example.exchangingprivatelessons.data.mapper.MessageMapper
import com.example.exchangingprivatelessons.data.remote.cloud.FunctionsDataSource
import com.example.exchangingprivatelessons.data.remote.dto.ChatDto
import com.example.exchangingprivatelessons.data.remote.firestore.FirestoreDataSource
import com.example.exchangingprivatelessons.data.repository.base.LiveSyncRepository
import com.example.exchangingprivatelessons.domain.model.Chat
import com.example.exchangingprivatelessons.domain.model.Message
import com.example.exchangingprivatelessons.domain.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth                              // ← NEW
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    /*  Data‑sources + DAO‑ים  */
    private val firestore : FirestoreDataSource,
    private val functions : FunctionsDataSource,
    private val chatDao   : ChatDao,
    private val userDao   : UserDao,            // ← NEW  (נדרש ל‑peerName)
    /*  Mappers  */
    private val chatMapper    : ChatMapper,
    private val messageMapper : MessageMapper,
    /*  Firebase  */
    private val auth : FirebaseAuth,            // ← NEW  (נדרש ל‑currentUid)
    /*  Dispatcher  */
    @IoDispatcher private val io : CoroutineDispatcher
) : LiveSyncRepository<ChatDto, ChatEntity, Chat>(), ChatRepository {

    /* ───────────── Live‑sync (Firestore → Room) ───────────── */

    override fun listenRemote() =
        firestore.listenChats()                          // Flow<Result<List<ChatDto>>>

    override suspend fun replaceAllLocal(list: List<ChatEntity>) =
        chatDao.replaceAll(list)

    override fun toEntity(dto: ChatDto)  = chatMapper.toEntity(dto)
    override fun toDomain(e: ChatEntity) = chatMapper.toDomain(e)

    /* ───────────── Streams ───────────── */

    /** כל הצ’אטים של המשתמש – כולל שם ה‑peer */
    override fun observeChats(): Flow<Result<List<Chat>>> =
        super.observe().map { res ->
            res.mapList { attachPeerName(it) }
        }

    /** צ’אט יחיד לפי ID (metadata בלבד) */
    override fun observeChat(chatId: String): Flow<Result<Chat>> =
        chatDao.observe(chatId).map { entity ->
            entity?.let { Result.Success(attachPeerName(chatMapper.toDomain(it))) }
                ?: Result.Failure(NoSuchElementException("Chat $chatId not found"))
        }

    /** הודעות של צ’אט מסוים (זרם נפרד) */
    override fun observeChatMessages(chatId: String): Flow<Result<List<Message>>> =
        firestore.observeChatMessages(chatId)            // Flow<List<MessageDto>>
            .map { list ->
                Result.Success(list.map(messageMapper::toDomain)) as Result<List<Message>>
            }
            .onStart { emit(Result.Loading) }
            .catch   { emit(Result.Failure(it)) }

    /* ───────────── Commands ───────────── */

    override suspend fun sendMessage(chatId: String, text: String): Result<Unit> =
        runCatching { functions.sendMessage(chatId, text) }
            .fold({ Result.Success(Unit) }, { Result.Failure(it) })

    override suspend fun createChat(peerUid: String): Result<String> =
        runCatching { functions.createChat(peerUid) }
            .fold({ Result.Success(it) }, { Result.Failure(it) })

    /** מאולץ – כבר לא נחוץ כי ה‑Flow מרענן לבד */
    override suspend fun forceRefreshChats(): Result<Unit> = Result.Success(Unit)

    /* ───────────── Helpers ───────────── */

    /** מוסיף לשיחה את שם‑הצד‑השני (אם יש במטמון; אין רשת כאן). */
    private suspend fun attachPeerName(chat: Chat): Chat {
        val myUid   = auth.currentUser?.uid ?: return chat
        val peerUid = chat.participantIds.firstOrNull { it != myUid } ?: return chat
        val peerName = userDao.get(peerUid)?.displayName ?: "Unknown"
        return chat.copy(peerName = peerName)
    }
}

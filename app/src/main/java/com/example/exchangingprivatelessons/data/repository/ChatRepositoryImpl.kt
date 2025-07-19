package com.example.exchangingprivatelessons.data.repository

import com.example.exchangingprivatelessons.common.di.IoDispatcher
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.data.local.dao.ChatDao
import com.example.exchangingprivatelessons.data.local.entity.ChatEntity
import com.example.exchangingprivatelessons.domain.model.Message
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.catch
import com.example.exchangingprivatelessons.data.mapper.ChatMapper
import com.example.exchangingprivatelessons.data.mapper.MessageMapper
import com.example.exchangingprivatelessons.data.remote.cloud.FunctionsDataSource
import com.example.exchangingprivatelessons.data.remote.dto.ChatDto
import com.example.exchangingprivatelessons.data.remote.dto.MessageDto
import com.example.exchangingprivatelessons.data.remote.firestore.FirestoreDataSource
import com.example.exchangingprivatelessons.data.repository.base.NetworkCacheRepository
import com.example.exchangingprivatelessons.domain.model.Chat
import com.example.exchangingprivatelessons.domain.repository.ChatRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

/* ─────────────────────────  ChatRepositoryImpl  ───────────────────────── */
@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirestoreDataSource,
    private val functions: FunctionsDataSource,
    private val dao: ChatDao,
    private val mapper: ChatMapper,
    private val messageMapper: MessageMapper,
    @IoDispatcher
    io: CoroutineDispatcher
) : NetworkCacheRepository<ChatEntity, ChatDto, Chat>(io), ChatRepository {

    override fun queryLocal() = dao.observeAll()

    override suspend fun fetchRemote(): List<ChatDto> = firestore.getChats()

    override suspend fun saveRemote(remote: List<ChatDto>) {
        dao.upsertAll(remote.map(mapper::toEntity))
    }

    override fun map(local: ChatEntity): Chat = mapper.toDomain(local)

    /* ───── Chat-specific API ───── */

    override fun observeChats(): Flow<Result<List<Chat>>> = observe()

    override fun observeChat(chatId: String): Flow<Result<Chat>> =
        dao.observe(chatId).map {
            it?.let { chatEntity ->
                Result.Success(mapper.toDomain(chatEntity))
            } ?: Result.Failure(NoSuchElementException("Chat $chatId not found"))
        }

    override suspend fun sendMessage(chatId: String, text: String): Result<Unit> =
        runCatching { functions.sendMessage(chatId, text) }
            .fold(
                onSuccess = { Result.Success(Unit) },
                onFailure = { Result.Failure(it) }
            )

    override suspend fun createChat(peerUid: String): Result<String> =
        runCatching { functions.createChat(peerUid) }
            .fold(
                onSuccess = { Result.Success(it) },
                onFailure = { Result.Failure(it) }
            )

    override suspend fun forceRefreshChats(): Result<Unit> =
        runCatching {
            val remote = fetchRemote()
            saveRemote(remote)
        }.fold(
            onSuccess = { Result.Success(Unit) },
            onFailure = { Result.Failure(it) }
        )

    override fun observeChatMessages(chatId: String): Flow<Result<List<Message>>> =
        firestore.observeChatMessages(chatId)               // Flow<List<MessageDto>>
            .map<List<MessageDto>, Result<List<Message>>> { dtos ->
                Result.Success(dtos.map(messageMapper::toDomain))
            }
            .onStart { emit(Result.Loading as Result<List<Message>>) }
            .catch   { emit(Result.Failure(it)  as Result<List<Message>>) }










}

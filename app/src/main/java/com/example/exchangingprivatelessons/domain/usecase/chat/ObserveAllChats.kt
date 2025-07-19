package com.example.exchangingprivatelessons.domain.usecase.chat

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.model.Chat
import com.example.exchangingprivatelessons.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveAllChats @Inject constructor(
    private val repo: ChatRepository
) {
    operator fun invoke(): Flow<Result<List<Chat>>> =
        repo.observeChats()
            .map { Result.Success(it) as Result<List<Chat>> }
            .onStart { emit(Result.Loading) }
}

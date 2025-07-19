package com.example.exchangingprivatelessons.domain.usecase.chat

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.model.Chat
import com.example.exchangingprivatelessons.domain.model.Message
import com.example.exchangingprivatelessons.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveChatMessages @Inject constructor(
    private val repo: ChatRepository
) {
    operator fun invoke(chatId: String) = repo.observeChatMessages(chatId)
}

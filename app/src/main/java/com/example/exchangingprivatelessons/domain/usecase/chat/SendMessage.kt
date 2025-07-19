package com.example.exchangingprivatelessons.domain.usecase.chat

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.repository.ChatRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SendMessage @Inject constructor(
    private val repo: ChatRepository,
    private val createChat: CreateChat
) {
    suspend operator fun invoke(
        chatId: String?,
        peerUid: String?,
        text: String
    ): Result<Unit> {
        val clean = text.trim()
        if (clean.isBlank())
            return Result.Failure(IllegalArgumentException("Message empty"))

        // ➊ יצירת צ'אט במידת הצורך
        val id = chatId ?: when (val res = createChat(peerUid)) {
            is Result.Success -> res.data
            is Result.Failure -> return Result.Failure(res.throwable)   // התאמת טיפוס
            else              -> return Result.Failure(IllegalStateException("Unexpected"))
        }

        // ➋ שליחת הודעה
        return repo.sendMessage(id, clean)
    }
}

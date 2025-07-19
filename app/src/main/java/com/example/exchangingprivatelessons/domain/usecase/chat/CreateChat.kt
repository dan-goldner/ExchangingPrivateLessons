package com.example.exchangingprivatelessons.domain.usecase.chat

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.repository.ChatRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateChat @Inject constructor(
    private val repo: ChatRepository
) {
    suspend operator fun invoke(peerUid: String?): Result<String> {
        if (peerUid.isNullOrBlank())
            return Result.Failure(IllegalArgumentException("peerUid required"))
        return repo.createChat(peerUid.trim())
    }
}

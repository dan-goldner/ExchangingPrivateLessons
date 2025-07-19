package com.example.exchangingprivatelessons.domain.usecase.chat

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.repository.ChatRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RefreshAllChats @Inject constructor(
    private val repo: ChatRepository
) {
    /** “נגיעה” בשרת – אותה פעולת fetch שקיימת כבר ב-repository  */
    suspend operator fun invoke(): Result<Unit> = repo.forceRefreshChats()
}

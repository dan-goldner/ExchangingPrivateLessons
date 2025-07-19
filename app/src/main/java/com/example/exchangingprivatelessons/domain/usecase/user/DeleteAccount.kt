package com.example.exchangingprivatelessons.domain.usecase.user

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteAccount @Inject constructor(
    private val repo: UserRepository
) {
    suspend operator fun invoke(): Result<Unit> = repo.deleteMyAccount()
}

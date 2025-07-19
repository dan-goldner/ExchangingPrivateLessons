package com.example.exchangingprivatelessons.domain.usecase.user

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.model.User
import com.example.exchangingprivatelessons.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveUser @Inject constructor(
    private val repo: UserRepository
) {
    /** זרם “אני” – תמיד המשתמש המחובר */
    operator fun invoke(): Flow<Result<User>> = repo.observeMe()
}

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
    /**
     * *uid == null* ➜ זרם “אני” (`observeMe`)
     * אחרת ➜ זרם משתמש לפי id
     */
    operator fun invoke(uid: String? = null): Flow<Result<User>> =
        if (uid == null) repo.observeMe() else repo.observeUser(uid)
}

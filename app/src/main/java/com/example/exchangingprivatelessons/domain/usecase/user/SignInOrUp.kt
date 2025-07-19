package com.example.exchangingprivatelessons.domain.usecase.user

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.model.User
import com.example.exchangingprivatelessons.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignInOrUp @Inject constructor(
    private val repo: UserRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        displayName: String? = null,
        bio: String? = null
    ): Result<User> =
        repo.signInOrUpWithEmail(email, password, displayName, bio)
}

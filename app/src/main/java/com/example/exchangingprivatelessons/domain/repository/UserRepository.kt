package com.example.exchangingprivatelessons.domain.repository

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    fun observeUser(uid: String): Flow<Result<User>>
    fun observeMe(): Flow<Result<User>>
    suspend fun getMe(): Result<User>
    suspend fun getUser(uid: String): Result<User>

    /** E‑mail / password auth – creates account if not existing. */
    suspend fun signInOrUpWithEmail(
        email: String,
        password: String,
        displayName: String? = null,
        bio: String? = null
    ): Result<User>


    suspend fun updateProfile(
        displayName: String? = null,
        bio: String? = null,
        photoUrl: String? = null
    ): Result<Unit>

    suspend fun deleteMyAccount(): Result<Unit>
    suspend fun touchLogin(): Result<Unit>

    fun currentUid(): String?
}

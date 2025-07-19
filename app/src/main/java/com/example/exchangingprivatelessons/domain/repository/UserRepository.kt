package com.example.exchangingprivatelessons.domain.repository

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    fun observeUser(uid: String): Flow<Result<User>>

    /** Observe current user (from local Room or Firestore). */
    fun observeMe(): Flow<Result<User>>

    /** Get current user once (from Room or remote fallback). */
    suspend fun getMe(): Result<User>

    fun currentUid(): String?

    /** Get another user by UID. */
    suspend fun getUser(uid: String): Result<User>

    /** E-mail / password auth – creates account if not existing. */
    suspend fun signInOrUpWithEmail(email: String, password: String): Result<User>

    suspend fun updateProfile(
        displayName: String? = null,
        bio: String? = null,
        photoUrl: String? = null
    ): Result<Unit>

    /** Deletes Firestore + Auth + Storage pic on server. */
    suspend fun deleteMyAccount(): Result<Unit>

    /** Ping server to stamp lastLoginAt (background job). */
    suspend fun touchLogin(): Result<Unit>
}

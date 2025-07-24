package com.example.exchangingprivatelessons.domain.repository

import android.net.Uri
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    /* Streams */
    fun observeUser(uid: String): Flow<Result<User>>
    fun observeMe():   Flow<Result<User>>

    /* One‑shot gets */
    suspend fun getMe(): Result<User>
    suspend fun getUser(uid: String): Result<User>

    suspend fun getUsers(ids: List<String>): Result<List<User>>


    /* Auth */
    suspend fun signInOrUpWithEmail(
        email: String, password: String,
        displayName: String? = null, bio: String? = null
    ): Result<User>

    /* Profile */
    suspend fun updateProfile(
        displayName: String? = null,
        bio: String? = null,
        photoUrl: String? = null
    ): Result<Unit>

    suspend fun updateAvatar(localFile: Uri): Result<String>
    suspend fun removeAvatar(): Result<Unit>

    /* House‑keeping */
    suspend fun deleteMyAccount(): Result<Unit>
    suspend fun touchLogin():     Result<Unit>

    /* NEW – local cleanup */
    suspend fun clearLocalUser()

    fun currentUid(): String?







}


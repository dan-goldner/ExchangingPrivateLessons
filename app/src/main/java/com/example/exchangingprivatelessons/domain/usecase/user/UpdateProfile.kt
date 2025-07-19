package com.example.exchangingprivatelessons.domain.usecase.user

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateProfile @Inject constructor(
    private val repo: UserRepository
) {
    suspend operator fun invoke(
        displayName: String? = null,
        bio: String? = null,
        photoUrl: String? = null
    ): Result<Unit> = repo.updateProfile(
        displayName = displayName,
        bio = bio,
        photoUrl = photoUrl
    )
}

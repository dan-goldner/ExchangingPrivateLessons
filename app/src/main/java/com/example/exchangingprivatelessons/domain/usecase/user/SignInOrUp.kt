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
        displayName: String? = null          // null ⇒ LOGIN, not null ⇒ SIGN-UP
    ): Result<User> =
        if (displayName == null)
            repo.signInOrUpWithEmail(email, password)           // LOGIN
        else {
            // SIGN-UP: אחרי יצירה מעדכנים שם תצוגה אם צריך
            repo.signInOrUpWithEmail(email, password).also { res ->
                if (res is Result.Success && displayName.isNotBlank())
                    repo.updateProfile(displayName = displayName)
            }
        }
}

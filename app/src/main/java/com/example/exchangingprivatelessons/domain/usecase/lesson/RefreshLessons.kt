package com.example.exchangingprivatelessons.domain.usecase.lesson

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.repository.LessonRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

/** Pull‑to‑refresh של רשימת השיעורים. */
@Singleton
class RefreshLessons @Inject constructor(
    private val repo: LessonRepository,
    private val auth: FirebaseAuth
) {
    suspend operator fun invoke(): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.Failure(
            IllegalStateException("User not logged in")
        )

        // Refresh both: general lessons + mine
        val r1 = repo.forceRefreshLessons()
        repo.refreshMineLessons(uid)

        return r1
    }
}
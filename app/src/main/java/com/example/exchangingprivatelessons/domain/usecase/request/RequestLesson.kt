package com.example.exchangingprivatelessons.domain.usecase.request

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.repository.LessonRequestRepository
import com.example.exchangingprivatelessons.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestLesson @Inject constructor(
    private val repo : LessonRequestRepository,
    private val user : UserRepository                         // ← הוזן
) {
    suspend operator fun invoke(lessonId: String, ownerId: String): Result<String> {

        val score = user.getCachedMe()?.score ?: 0            // Room – בלי רשת

        if (score <= -3) {                                    // 🔒 חסימת‑קליינט
            return Result.Failure(
                IllegalStateException("LOW_SCORE_LOCAL")      // ViewModel יטפל
            )
        }

        return repo.requestLesson(lessonId, ownerId)
    }
}

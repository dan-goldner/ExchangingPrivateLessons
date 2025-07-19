package com.example.exchangingprivatelessons.domain.usecase.rating

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.repository.RatingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RateLesson @Inject constructor(
    private val repo: RatingRepository
) {
    suspend operator fun invoke(
        lessonId: String,
        value: Int,
        comment: String? = null
    ): Result<Unit> {
        if (lessonId.isBlank())
            return Result.Failure(IllegalArgumentException("Lesson ID is required"))
        if (value !in 1..5)
            return Result.Failure(IllegalArgumentException("Rating must be between 1 and 5"))
        return repo.rateLesson(lessonId.trim(), value, comment?.trim())
    }
}

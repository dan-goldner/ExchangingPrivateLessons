package com.example.exchangingprivatelessons.domain.usecase.request

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.repository.LessonRequestRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestLesson @Inject constructor(
    private val repo: LessonRequestRepository
) {

    suspend fun requestLesson(lessonId: String, ownerId: String): Result<String> {
        if (lessonId.isBlank() || ownerId.isBlank())
            return Result.Failure(IllegalArgumentException("Invalid IDs"))
        return repo.requestLesson(lessonId, ownerId)
    }
}

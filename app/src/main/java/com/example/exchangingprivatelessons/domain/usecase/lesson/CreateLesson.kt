package com.example.exchangingprivatelessons.domain.usecase.lesson

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.repository.LessonRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateLesson @Inject constructor(
    private val repo: LessonRepository
) {
    suspend operator fun invoke(
        title: String,
        description: String,
    ): Result<String> {
        if (title.isBlank() || description.isBlank())
            return Result.Failure(IllegalArgumentException("Title and description are required"))
        return repo.createLesson(title.trim(), description.trim())
    }
}

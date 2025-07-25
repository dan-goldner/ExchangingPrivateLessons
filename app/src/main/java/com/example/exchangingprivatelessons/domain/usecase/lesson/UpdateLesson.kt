package com.example.exchangingprivatelessons.domain.usecase.lesson

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.repository.LessonRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateLesson @Inject constructor(
    private val repo: LessonRepository
) {
    suspend operator fun invoke(
        lessonId: String,
        title: String? = null,
        description: String? = null,
    ): Result<Unit> {
        if (lessonId.isBlank())
            return Result.Failure(IllegalArgumentException("Lesson ID is required"))
        if (title == null && description == null)
            return Result.Failure(IllegalArgumentException("Nothing to update"))

        return repo.updateLesson(
            lessonId = lessonId.trim(),
            title = title?.trim(),
            description = description?.trim()
        )
    }
}

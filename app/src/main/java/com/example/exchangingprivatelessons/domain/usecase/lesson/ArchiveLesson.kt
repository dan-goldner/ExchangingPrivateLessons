package com.example.exchangingprivatelessons.domain.usecase.lesson

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.repository.LessonRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArchiveLesson @Inject constructor(
    private val repo: LessonRepository
) {
    suspend operator fun invoke(lessonId: String, archived: Boolean): Result<Unit> =
        repo.archiveLesson(lessonId, archived)
}

package com.example.exchangingprivatelessons.domain.usecase.lesson

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.repository.LessonRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Pull‑to‑refresh של רשימת השיעורים. */
@Singleton
class RefreshLessons @Inject constructor(
    private val repo: LessonRepository
) {
    suspend operator fun invoke(): Result<Unit> = repo.forceRefreshLessons()
}

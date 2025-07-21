package com.example.exchangingprivatelessons.domain.usecase.lesson

import com.example.exchangingprivatelessons.domain.repository.LessonRepository
import javax.inject.Inject

class RefreshMineLessons @Inject constructor(
    private val repo: LessonRepository
) {
    suspend operator fun invoke(userId: String) {
        repo.refreshMineLessons(userId)
    }
}
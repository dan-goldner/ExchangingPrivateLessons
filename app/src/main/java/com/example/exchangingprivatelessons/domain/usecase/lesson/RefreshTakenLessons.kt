package com.example.exchangingprivatelessons.domain.usecase.lesson

import com.example.exchangingprivatelessons.domain.repository.TakenLessonRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RefreshTakenLessons @Inject constructor(
    private val repo: TakenLessonRepository
) {
    suspend operator fun invoke() {
        repo.refresh()
    }
}
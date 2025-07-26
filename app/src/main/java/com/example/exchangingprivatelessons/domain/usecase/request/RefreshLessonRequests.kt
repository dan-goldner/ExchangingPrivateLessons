package com.example.exchangingprivatelessons.domain.usecase.request

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.repository.LessonRequestRepository
import javax.inject.Inject
import javax.inject.Singleton

/** רענון ידני של בקשות שיעור (בעל שיעור או תלמיד). */
@Singleton
class RefreshLessonRequests @Inject constructor(
    private val repo: LessonRequestRepository
) {
    suspend operator fun invoke(): Result<Unit> = repo.forceRefreshLessonRequests()
}
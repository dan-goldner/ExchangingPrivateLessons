package com.example.exchangingprivatelessons.domain.usecase.request

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.model.LessonRequest
import com.example.exchangingprivatelessons.domain.repository.LessonRequestRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * מאזין לבקשות שיעור נכנסות (כל השיעורים שאתה בעליהם).
 */
@Singleton
class ObserveIncomingRequests @Inject constructor(
    private val repo: LessonRequestRepository
) {
    operator fun invoke(): Flow<Result<List<LessonRequest>>> =
        repo.observeIncomingRequests()
}

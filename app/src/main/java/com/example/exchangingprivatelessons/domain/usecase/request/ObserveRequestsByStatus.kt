package com.example.exchangingprivatelessons.domain.usecase.request

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.model.LessonRequest
import com.example.exchangingprivatelessons.domain.model.RequestStatus
import com.example.exchangingprivatelessons.domain.repository.LessonRequestRepository
import com.example.exchangingprivatelessons.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class ObserveRequestsByStatus @Inject constructor(
    private val repo: LessonRequestRepository,
) {
    operator fun invoke(status: RequestStatus): Flow<Result<List<LessonRequest>>> =
        repo.observeRequestsByStatus(repo.currentUid() ?: "", status.name)
}
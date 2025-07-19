package com.example.exchangingprivatelessons.domain.repository

import com.example.exchangingprivatelessons.domain.model.LessonRequest
import kotlinx.coroutines.flow.Flow
import com.example.exchangingprivatelessons.common.util.Result

interface LessonRequestRepository {

    fun currentUid(): String?
    suspend fun requestLesson(lessonId: String, ownerId: String): Result<String>
    suspend fun approveRequest(requestId: String): Result<Unit>
    suspend fun declineRequest(requestId: String): Result<Unit>
    suspend fun cancelRequest(requestId: String): Result<Unit>

    suspend fun getMyRequest(lessonId: String, myUid: String): Result<LessonRequest?>
    fun observeIncomingRequests(): Flow<Result<List<LessonRequest>>>          // Owner
    fun observeRequestsByStatus(uid: String, status: String): Flow<Result<List<LessonRequest>>>
    suspend fun forceRefreshLessonRequests(): Result<Unit>
}

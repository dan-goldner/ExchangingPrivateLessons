package com.example.exchangingprivatelessons.data.repository

import com.example.exchangingprivatelessons.common.di.IoDispatcher
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.data.local.dao.LessonRequestDao
import com.example.exchangingprivatelessons.data.local.entity.LessonRequestEntity
import com.example.exchangingprivatelessons.data.mapper.LessonRequestMapper
import com.example.exchangingprivatelessons.data.remote.cloud.FunctionsDataSource
import com.example.exchangingprivatelessons.data.remote.dto.LessonRequestDto
import com.example.exchangingprivatelessons.data.remote.firestore.FirestoreDataSource
import com.example.exchangingprivatelessons.data.repository.base.NetworkCacheRepository
import com.example.exchangingprivatelessons.domain.model.LessonRequest
import com.example.exchangingprivatelessons.domain.repository.LessonRequestRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRequestRepositoryImpl @Inject constructor(
    private val firestore: FirestoreDataSource,
    private val functions: FunctionsDataSource,
    private val dao: LessonRequestDao,
    private val auth: FirebaseAuth,
    private val mapper: LessonRequestMapper,
    @IoDispatcher
    private val io: CoroutineDispatcher
) : NetworkCacheRepository<LessonRequestEntity, LessonRequestDto, LessonRequest>(io),
    LessonRequestRepository {


    override fun queryLocal()                       = dao.observeAll()
    override suspend fun fetchRemote()              = firestore.getLessonRequests()
    override suspend fun saveRemote(remote: List<LessonRequestDto>) =
        dao.upsertAll(remote.map(mapper::toEntity))
    override fun map(local: LessonRequestEntity)    = mapper.toDomain(local)

    /* ───── API ───── */

    override fun observeRequestsByStatus(
        uid: String,
        status: String
    ): Flow<Result<List<LessonRequest>>> =
        dao.observeByStatus(uid, status)
            .map { Result.Success(it.map(mapper::toDomain)) }


    /* incoming for owner */
    override fun observeIncomingRequests(): Flow<Result<List<LessonRequest>>> {
        val ownerUid = auth.currentUser?.uid
            ?: return flowOf(Result.Failure(IllegalStateException("User not logged in")))
        return dao.observeIncoming(ownerUid)
            .map { Result.Success(it.map(mapper::toDomain)) }
    }


    /* refresh */
    override suspend fun forceRefreshLessonRequests(): Result<Unit> =
        runCatching {
            val remote = firestore.getLessonRequests()
            dao.upsertAll(remote.map(mapper::toEntity))
        }.fold(
            onSuccess = { Result.Success(Unit) },
            onFailure = { Result.Failure(it) }
        )

    // LessonRequestRepositoryImpl.kt
    override suspend fun getMyRequest(
        lessonId: String,
        myUid: String
    ): Result<LessonRequest?> =
        dao.getMyRequest(lessonId, myUid)
            ?.let { Result.Success(mapper.toDomain(it)) }
            ?: Result.Success(null)

    override fun currentUid(): String? = auth.currentUser?.uid



    override suspend fun requestLesson(
        lessonId: String,
        ownerId: String
    ): Result<String> =
        runCatching { functions.createLessonRequest(lessonId, ownerId) }
            .fold(
                onSuccess = { Result.Success(it) },
                onFailure = { Result.Failure(it) }
            )

    override suspend fun approveRequest(requestId: String): Result<Unit> =
        runCatching { functions.approveLessonRequest(requestId) }
            .fold(
                onSuccess = { Result.Success(Unit) },
                onFailure = { Result.Failure(it) }
            )

    override suspend fun declineRequest(requestId: String): Result<Unit> =
        runCatching { functions.declineLessonRequest(requestId) }
            .fold(
                onSuccess = { Result.Success(Unit) },
                onFailure = { Result.Failure(it) }
            )

    override suspend fun cancelRequest(requestId: String): Result<Unit> =
        runCatching { functions.cancelLessonRequest(requestId) }
            .fold(
                onSuccess = { Result.Success(Unit) },
                onFailure = { Result.Failure(it) }
            )
}

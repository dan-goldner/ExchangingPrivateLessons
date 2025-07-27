package com.example.exchangingprivatelessons.data.repository

import android.util.Log
import com.example.exchangingprivatelessons.common.di.ApplicationScope
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.flowOn
import com.example.exchangingprivatelessons.domain.repository.LessonRequestRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRequestRepositoryImpl @Inject constructor(
    private val firestore : FirestoreDataSource,
    private val functions : FunctionsDataSource,
    private val dao       : LessonRequestDao,
    private val auth      : FirebaseAuth,
    private val mapper    : LessonRequestMapper,
    @IoDispatcher private val io: CoroutineDispatcher,
    @ApplicationScope private val appScope: CoroutineScope
) : LessonRequestRepository {



    private var job: Job? = null

    init { startLiveSync() }

    private fun startLiveSync() {
        if (job != null) return
        val uid = auth.uidOrCrash()

        job = firestore.listenLessonRequests(uid)
            .mapNotNull { (it as? Result.Success)?.data }
            .onEach { remote ->
                dao.replaceAllForUser(
                    uid,
                    remote.map(mapper::toEntity)
                )
            }
            .flowOn(io)
            .launchIn(appScope)
    }



    /* ───────── Public streams (Room בלבד) ───────── */

    override fun observeIncomingRequests(): Flow<Result<List<LessonRequest>>> =
        dao.observeIncoming(auth.uidOrCrash())
            .map { Result.Success(it.map(mapper::toDomain)) }

    override fun observeRequestsByStatus(
        uid: String,
        status: String?
    ): Flow<Result<List<LessonRequest>>> =
        dao.observeByStatus(uid, status)
            .map { Result.Success(it.map(mapper::toDomain)) }


    override suspend fun forceRefreshLessonRequests(): Result<Unit> = withContext(io) {
        runCatching {
            val uid      = auth.uidOrCrash()
            val remote   = firestore.getLessonRequests()
            Log.d("DBG/Remote", "Got ${remote.size} requests")
            dao.replaceAllForUser(uid, remote.map(mapper::toEntity))
        }.fold({ Result.Success(Unit) }, { Result.Failure(it) })
    }


    /* ───────── Mutations – כתיבה לענן + עדכון Room ───────── */

    override suspend fun requestLesson(
        lessonId: String, ownerId: String
    ): Result<String> = withContext(io) {
        runCatching {
            val id   = functions.createLessonRequest(lessonId, ownerId)
            val dto  = firestore.getLessonRequest(id)           // fetch יחיד
            dao.upsert(mapper.toEntity(dto))                    // Cache‑hit מיידי
            id
        }.fold({ Result.Success(it) }, { Result.Failure(it) })
    }

    override suspend fun approveRequest(id: String)  = mutate(id) { functions.approveLessonRequest(id) }
    override suspend fun declineRequest(id: String)  = mutate(id) { functions.declineLessonRequest(id) }

    override suspend fun cancelRequest(id: String) = withContext(io) {
        runCatching {
            functions.cancelLessonRequest(id)   // מוחק בענן
            dao.delete(id)                      // מוחק מה‑Room, לא מנסה fetch
        }.fold({ Result.Success(Unit) }, { Result.Failure(it) })
    }


    private suspend fun mutate(id: String, call: suspend () -> Unit): Result<Unit> = withContext(io) {
        runCatching {
            call()
            val dto = firestore.getLessonRequest(id)
            dao.upsert(mapper.toEntity(dto))          // תמיד מעדכן את ה‑Cache
        }.fold({ Result.Success(Unit) }, { Result.Failure(it) })
    }

    /* ───────── Misc ───────── */
    override suspend fun getMyRequest(lessonId: String, myUid: String) =
        dao.getMyRequest(lessonId, myUid)
            ?.let { Result.Success(mapper.toDomain(it)) }
            ?: Result.Success(null)

    override fun currentUid() = auth.currentUser?.uid
    private fun FirebaseAuth.uidOrCrash() =
        currentUser?.uid ?: throw IllegalStateException("Not logged‑in")




}

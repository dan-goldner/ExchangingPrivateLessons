package com.example.exchangingprivatelessons.data.repository

import android.util.Log
import com.example.exchangingprivatelessons.common.di.IoDispatcher
import com.example.exchangingprivatelessons.data.local.dao.TakenLessonDao
import com.example.exchangingprivatelessons.data.local.entity.TakenLessonEntity
import com.example.exchangingprivatelessons.data.mapper.LessonMapper
import com.example.exchangingprivatelessons.data.mapper.TakenLessonMapper
import com.example.exchangingprivatelessons.data.remote.cloud.FunctionsDataSource
import com.example.exchangingprivatelessons.data.remote.dto.TakenLessonDto
import com.example.exchangingprivatelessons.data.remote.firestore.FirestoreDataSource
import com.example.exchangingprivatelessons.data.repository.base.NetworkCacheRepository
import com.example.exchangingprivatelessons.domain.model.LessonStatus
import com.example.exchangingprivatelessons.domain.model.RequestStatus
import com.example.exchangingprivatelessons.common.util.Result
import kotlinx.coroutines.flow.*

import com.example.exchangingprivatelessons.domain.model.TakenLesson
import com.example.exchangingprivatelessons.domain.repository.TakenLessonRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton



/* ────────────────────  TakenLessonRepositoryImpl  ─────────────────────── */
@Singleton
class TakenLessonRepositoryImpl @Inject constructor(
    private val firestore   : FirestoreDataSource,
    private val functions   : FunctionsDataSource,   // ← שמרנו, אולי תצטרך בהמשך
    private val dao         : TakenLessonDao,
    private val mapper      : TakenLessonMapper,
    private val lessonMapper: LessonMapper,
    private val auth        : FirebaseAuth,
    @IoDispatcher private val io: CoroutineDispatcher
) : NetworkCacheRepository<TakenLessonEntity, TakenLessonDto, TakenLesson>(io),
    TakenLessonRepository {

    /* ---------- Realtime sync ---------- */
    private var syncJob: Job? = null
    init { startRealtimeSync() }

    private fun startRealtimeSync() {
        if (syncJob != null) return           // כבר רץ
        val uid = auth.currentUser?.uid ?: return

        syncJob = firestore.listenTakenLessons(uid)
            .mapNotNull { (it as? Result.Success)?.data }
            .onEach { docs ->
                val enriched = enrichDtos(docs)           // suspend
                dao.clearAll()
                dao.upsertAll(enriched.map(mapper::dtoToEntity))
            }
            .flowOn(io)
            .launchIn(CoroutineScope(io))
    }

    /* enrich בונה DTO עשיר לשכבת ה‑UI */
    private suspend fun enrichDtos(docs: List<TakenLessonDto>): List<TakenLessonDto> =
        coroutineScope {
            docs.map { doc ->
                async(io) {
                    val lesson = firestore.getLessonById(doc.lessonId) ?: return@async null
                    val ownerName     = firestore.getUserName(lesson.ownerId)
                    val ownerPhotoUrl = firestore.getUserPhotoUrl(lesson.ownerId)

                    doc.copy(
                        lesson        = lesson,
                        canRate       = false,
                        ownerName     = ownerName,
                        ownerPhotoUrl = ownerPhotoUrl
                    )
                }
            }.awaitAll().filterNotNull()
        }


    /* ---------- Network‑Bound API ---------- */
    // ה‑observe() יורש מ‑NetworkCacheRepository, כך שהנתונים
    // יתדחפו אוטומטית ל‑UI כשה‑dao מתעדכן.

    override fun queryLocal() = dao.observeAll()
    override suspend fun fetchRemote() = emptyList<TakenLessonDto>() // לא נדרש עוד
    override suspend fun saveRemote(list: List<TakenLessonDto>) { /* no‑op */ }
    override fun map(local: TakenLessonEntity) = mapper.toDomain(local)

    /* ריענון ידני (למשתמש “גרור‑לרענן”) */
    override suspend fun refresh() = withContext(io) {
        startRealtimeSync()      // מוודא שסנכרון רץ
    }

    override fun observeTakenLessons() = observe()
}

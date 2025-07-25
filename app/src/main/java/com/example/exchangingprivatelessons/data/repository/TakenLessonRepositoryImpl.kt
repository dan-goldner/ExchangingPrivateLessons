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
import com.example.exchangingprivatelessons.domain.model.TakenLesson
import com.example.exchangingprivatelessons.domain.repository.TakenLessonRepository
import kotlinx.coroutines.CoroutineDispatcher

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton



/* ────────────────────  TakenLessonRepositoryImpl  ─────────────────────── */
@Singleton
class TakenLessonRepositoryImpl @Inject constructor(
    private val firestore: FirestoreDataSource,
    private val function: FunctionsDataSource,
    private val dao: TakenLessonDao,
    private val mapper: TakenLessonMapper,
    private val lessonMapper: LessonMapper,
    @IoDispatcher
    private val io: CoroutineDispatcher
) : NetworkCacheRepository<TakenLessonEntity, TakenLessonDto, TakenLesson>(
    io
), TakenLessonRepository {

    override fun queryLocal()                  = dao.observeAll()
    /*override suspend fun fetchRemote()         = firestore.getTakenLessons()*/

    override suspend fun fetchRemote(): List<TakenLessonDto> {
        val currentUid = firestore.getCurrentUserId() ?: return emptyList()

        val approvedRequests = firestore.getLessonRequests()
            .filter { it.requesterId == currentUid && it.status == RequestStatus.Approved }
            .distinctBy { it.lessonId }

        val lessonIds = approvedRequests.map { it.lessonId }

        return lessonIds.mapNotNull { lessonId ->
            val lesson = firestore.getLessonById(lessonId)
            Log.d("TakenLessonRepo", "Lesson [$lessonId] → ${lesson?.title} | status = ${lesson?.status}")

            if (lesson != null && lesson.status != LessonStatus.Archived) {
                val req = approvedRequests.firstOrNull { it.lessonId == lessonId }
                TakenLessonDto(
                    lesson = lesson,
                    takenAt = req?.requestedAt,
                    canRate = false
                )
            } else null
        }
    }

    override suspend fun saveRemote(remote: List<TakenLessonDto>) {
        val entities = remote.map(mapper::dtoToEntity)   // ← שם‑הפונקציה ייחודי
        dao.upsertAll(entities)
    }

    override fun map(local: TakenLessonEntity) = mapper.toDomain(local)

    override fun observeTakenLessons() = observe()
}

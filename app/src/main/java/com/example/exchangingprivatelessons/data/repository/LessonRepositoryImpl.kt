package com.example.exchangingprivatelessons.data.repository

import com.example.exchangingprivatelessons.common.di.IoDispatcher
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.data.local.dao.LessonDao
import com.example.exchangingprivatelessons.data.local.entity.LessonEntity
import com.example.exchangingprivatelessons.data.mapper.LessonMapper
import com.example.exchangingprivatelessons.data.remote.cloud.FunctionsDataSource
import com.example.exchangingprivatelessons.data.remote.dto.LessonDto
import com.example.exchangingprivatelessons.data.remote.dto.RatingDto
import com.example.exchangingprivatelessons.data.remote.firestore.FirestoreDataSource
import com.example.exchangingprivatelessons.data.repository.base.NetworkCacheRepository
import com.example.exchangingprivatelessons.domain.model.Lesson
import com.example.exchangingprivatelessons.domain.model.Rating
import com.example.exchangingprivatelessons.domain.repository.LessonRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRepositoryImpl @Inject constructor(
    private val firestore: FirestoreDataSource,
    private val functions: FunctionsDataSource,
    private val dao: LessonDao,
    private val mapper: LessonMapper,
    private val auth: FirebaseAuth,
    @IoDispatcher
    private val io: CoroutineDispatcher
) : NetworkCacheRepository<LessonEntity, LessonDto, Lesson>(io), LessonRepository {

    override fun queryLocal() = dao.observeActive()

    override suspend fun fetchRemote() = firestore.getLessons()

    override suspend fun saveRemote(remote: List<LessonDto>) {
        dao.upsertAll(remote.map(mapper::toEntity))   // בלי named‑args
    }





    override suspend fun getLesson(lessonId: String): Result<Lesson> = withContext(io) {
        dao.get(lessonId)?.let { return@withContext Result.Success(mapper.toDomain(it)) }

        // אין ב‑Room → נסה Firestore
        runCatching {
            firestore.getLessons().first { it.id == lessonId }
        }.fold(
            onSuccess = { dto ->
                val entity = mapper.toEntity(dto)
                dao.upsert(entity)                         // cache
                Result.Success(mapper.toDomain(entity))
            },
            onFailure = { Result.Failure(it) }
        )
    }



    override fun map(local: LessonEntity) = mapper.toDomain(local)

    /* ───── Lesson-specific API ───── */

    override fun observeLessons(onlyMine: Boolean): Flow<Result<List<Lesson>>> =
        if (onlyMine) {
            val uid = auth.currentUser?.uid ?: return flowOf(Result.Failure(
                IllegalStateException("User not logged in")
            ))
            dao.observeMine(uid)
                .map { Result.Success(it.map(mapper::toDomain)) }
        } else {
            observe()      // NetworkCacheRepository.observe()
        }

    override fun observeLesson(lessonId: String): Flow<Result<Lesson>> =
        dao.observe(lessonId).map { entity ->
            entity?.let { Result.Success(mapper.toDomain(it)) }
                ?: Result.Failure(NullPointerException("Lesson not found"))
        }

    override suspend fun forceRefreshLessons(): Result<Unit> =
        runCatching {
            val remote = firestore.getLessons()
            dao.upsertAll(remote.map(mapper::toEntity))
        }.fold(
            onSuccess = { Result.Success(Unit) },
            onFailure = { Result.Failure(it) }
        )

    override suspend fun updateLesson(
        lessonId: String,
        title: String?,
        description: String?,
        imageUrl: String?
    ): Result<Unit> =
        runCatching {
            functions.updateLesson(lessonId, title, description, imageUrl)
        }.fold(
            onSuccess = { Result.Success(Unit) },
            onFailure = { Result.Failure(it) }
        )


    override suspend fun archiveLesson(lessonId: String, archived: Boolean): Result<Unit> =
        runCatching {
            functions.archiveLesson(lessonId, archived)
        }.fold(
            onSuccess = { Result.Success(Unit) },
            onFailure = { Result.Failure(it) }
        )


    override suspend fun createLesson(
        title: String,
        description: String,
        imageUrl: String?
    ): Result<String> =
        runCatching { functions.createLesson(title, description, imageUrl) }
            .fold(
                onSuccess = { Result.Success(it) },
                onFailure = { Result.Failure(it) }
            )
}

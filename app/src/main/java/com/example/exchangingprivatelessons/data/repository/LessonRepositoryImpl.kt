package com.example.exchangingprivatelessons.data.repository

import android.util.Log
import com.example.exchangingprivatelessons.common.di.IoDispatcher
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.data.local.dao.LessonDao
import com.example.exchangingprivatelessons.data.local.dao.LessonRequestDao
import com.example.exchangingprivatelessons.data.local.entity.LessonEntity
import com.example.exchangingprivatelessons.data.mapper.LessonMapper
import com.example.exchangingprivatelessons.data.remote.cloud.FunctionsDataSource
import com.example.exchangingprivatelessons.data.remote.dto.LessonDto
import com.example.exchangingprivatelessons.data.remote.firestore.FirestoreDataSource
import com.example.exchangingprivatelessons.data.repository.base.NetworkCacheRepository
import com.example.exchangingprivatelessons.domain.model.Lesson
import com.example.exchangingprivatelessons.domain.repository.LessonRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRepositoryImpl @Inject constructor(
    private val firestore: FirestoreDataSource,
    private val functions: FunctionsDataSource,
    private val dao: LessonDao,
    private val lessonRequestDao: LessonRequestDao, // added this clearly
    private val mapper: LessonMapper,
    private val auth: FirebaseAuth,
    @IoDispatcher
    private val io: CoroutineDispatcher
) : NetworkCacheRepository<LessonEntity, LessonDto, Lesson>(io), LessonRepository {

    override fun queryLocal() = dao.observeActive()

    override suspend fun fetchRemote() = firestore.getLessons()

    override suspend fun saveRemote(remote: List<LessonDto>) {
        dao.upsertAll(remote.map(mapper::toEntity))
    }

    override suspend fun getLesson(lessonId: String): Result<Lesson> = withContext(io) {
        dao.get(lessonId)?.let { return@withContext Result.Success(mapper.toDomain(it)) }

        runCatching {
            firestore.getLessons().first { it.id == lessonId }
        }.fold(
            onSuccess = { dto ->
                val entity = mapper.toEntity(dto)
                dao.upsert(entity)
                Result.Success(mapper.toDomain(entity))
            },
            onFailure = { Result.Failure(it) }
        )
    }

    override fun map(local: LessonEntity) = mapper.toDomain(local)

    override fun observeLessons(onlyMine: Boolean): Flow<Result<List<Lesson>>> {
        return if (onlyMine) {
            val uid = auth.currentUser?.uid ?: return flowOf(
                Result.Failure(IllegalStateException("User not logged in"))
            )
            dao.observeMine(uid)
                .map { lessons ->
                    lessons.filter { it.id.isNotBlank() } // 👈 FILTER HERE
                }
                .onEach { lessons ->
                    Log.d("LessonRepo", "observeMine returned ${lessons.size} lessons (filtered)")
                    lessons.forEach {
                        Log.d("LessonRepo", "LessonEntity from DB: id=${it.id}, title=${it.title}, createdAt=${it.createdAt}")
                    }
                }
                .map { Result.Success(it.map(mapper::toDomain)) }
        } else {
            observe().map { result ->
                if (result is Result.Success) {
                    Result.Success(result.data.filter { it.id.isNotBlank() })
                } else result
            }
        }
    }

    override fun observeLesson(lessonId: String): Flow<Result<Lesson>> =
        dao.observe(lessonId).map { entity ->
            entity?.let { Result.Success(mapper.toDomain(it)) }
                ?: Result.Failure(NullPointerException("Lesson not found"))
        }

    override suspend fun forceRefreshLessons(): Result<Unit> = runCatching {
        val remote = firestore.getLessons()
            dao.upsertAll(remote.map(mapper::toEntity))
        }.fold(
            onSuccess = { Result.Success(Unit) },
            onFailure = { Result.Failure(it) }
        )

    override fun observeTakenLessons(userId: String): Flow<Result<List<Lesson>>> {
        return lessonRequestDao.observeTakenByUser(userId)
            .flatMapLatest { lessonIds ->
                if (lessonIds.isEmpty()) {
                    flowOf<Result<List<Lesson>>>(Result.Success(emptyList()))
                } else {
                    dao.observeLessonsByIds(lessonIds)
                        .map { entities ->
                            Result.Success(entities.map(mapper::toDomain))
                        }
                }
            }
            .catch { e ->
                emit(Result.Failure(e))  // valid only if return type is Flow<Result<List<Lesson>>>
            }
    }

    override suspend fun updateLesson(
        lessonId: String,
        title: String?,
        description: String?,
        imageUrl: String?
    ): Result<Unit> = runCatching {
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
    ): Result<String> = runCatching {
        val newId = functions.createLesson(title, description, imageUrl)

        // Retry getting the lesson directly from Firestore by ID
        var dto: LessonDto? = null
        repeat(10) { attempt ->
            dto = firestore.getLessonById(newId)
            if (dto != null) return@repeat
            kotlinx.coroutines.delay(500L)
        }

        dto ?: throw IllegalStateException("Created lesson not found in Firestore (getLessonById)")

        val currentUid = auth.currentUser?.uid ?: ""
        val entity = mapper.toEntity(dto!!).copy(ownerId = currentUid)

        Log.d("LessonRepo", "Creating lesson: id=${entity.id}, ownerId=${entity.ownerId}, status=${entity.status}, createdAt=${entity.createdAt}")
        Log.d("Repo", "Lesson saved locally: $entity")
        dao.upsert(entity)

        Result.Success(newId)
    }.getOrElse {
        Log.e("Repo", "Failed to create lesson: ${it.localizedMessage}")
        Result.Failure(it)
    }

    override suspend fun refreshMineLessons(userId: String) {
        val remote = firestore.getLessonsOfferedByUser(userId)
        dao.upsertAll(remote.map(mapper::toEntity))
    }
}
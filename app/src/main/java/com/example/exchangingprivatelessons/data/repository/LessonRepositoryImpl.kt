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
import com.example.exchangingprivatelessons.domain.model.LessonStatus
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

    // LessonRepositoryImpl.saveRemote(...)
    override suspend fun saveRemote(remote: List<LessonDto>) {
        remote.forEach {
            Log.d("DBG/SaveRemote", "→ upsert ${it.id} owner=${it.ownerId} status=${it.status}")
        }
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

    // LessonRepositoryImpl.observeLessons(...)
    override fun observeLessons(onlyMine: Boolean): Flow<Result<List<Lesson>>> {
        val uid = auth.currentUser?.uid.orEmpty()
        return if (onlyMine) {
            dao.observeMine(uid)           // ללא סינון נוסף
                .map { Result.Success(it.map(mapper::toDomain)) }
        } else {
            observe()                      // observeActive()
                .onEach { res ->
                    if (res is Result.Success)
                        Log.d("DBG/RepoRaw", "before filter size=${res.data.size}")
                }
                .map { res ->
                    if (res is Result.Success) {
                        val filtered = res.data.filter { it.ownerId != uid }
                        Log.d("DBG/RepoRaw", "after filter size=${filtered.size}")
                        Result.Success(filtered)
                    } else res
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
    ): Result<Unit> = runCatching {
        functions.updateLesson(lessonId, title, description)
        Unit  // explicitly return Unit instead of trying to cast result
    }.fold(
        onSuccess = { Result.Success(it) },
        onFailure = { Result.Failure(it) }
    )

    override suspend fun deleteLesson(lessonId: String): Result<Unit> {
        return runCatching {
            dao.delete(lessonId)
            Log.d("DeleteLesson", "Deleted from local DB: $lessonId")
            firestore.deleteLesson(lessonId)
            Log.d("DeleteLesson", "Deleted from Firestore: $lessonId")
        }.fold(
            onSuccess = { Result.Success(Unit) },
            onFailure = {
                Log.e("DeleteLesson", "Error deleting lesson", it)
                Result.Failure(it)
            }
        )
    }


    override suspend fun archiveLesson(
        lessonId: String,
        archived: Boolean
    ): Result<Unit> = withContext(io) {
        val localStatus = if (archived) LessonStatus.Archived.name
        else           LessonStatus.Active.name

        /* ① עדכון מקומי מידי */
        dao.setStatus(lessonId, localStatus)

        /* ② ניסיון עדכון בשרת */
        runCatching {
            functions.archiveLesson(lessonId, archived)
        }.fold(
            onSuccess = { Result.Success(Unit) },
            onFailure = { e ->
                Log.e("ArchiveLesson", "Cloud Function failed", e)
                val rollbackStatus = if (archived) LessonStatus.Active.name
                else           LessonStatus.Archived.name
                dao.setStatus(lessonId, rollbackStatus)
                Result.Failure(e)
            }
        )
    }


    override suspend fun createLesson(
        title: String,
        description: String,
    ): Result<String> = runCatching {
        val newId = functions.createLesson(title, description)

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
/* ───────────────────────  RatingRepositoryImpl  ────────────────────── */
package com.example.exchangingprivatelessons.data.repository

import com.example.exchangingprivatelessons.common.di.IoDispatcher
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.data.local.dao.RatingDao
import com.example.exchangingprivatelessons.data.local.entity.RatingEntity
import com.example.exchangingprivatelessons.data.mapper.RatingMapper
import com.example.exchangingprivatelessons.data.remote.cloud.FunctionsDataSource
import com.example.exchangingprivatelessons.data.remote.dto.RatingDto
import com.example.exchangingprivatelessons.data.remote.firestore.FirestoreDataSource
import com.example.exchangingprivatelessons.data.repository.base.NetworkCacheRepository
import com.example.exchangingprivatelessons.domain.model.Rating
import com.example.exchangingprivatelessons.domain.repository.RatingRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RatingRepositoryImpl @Inject constructor(
    private val firestore: FirestoreDataSource,
    private val functions: FunctionsDataSource,
    private val dao: RatingDao,
    private val mapper: RatingMapper,
    @IoDispatcher private val io: CoroutineDispatcher
) : NetworkCacheRepository<RatingEntity, RatingDto, Rating>(io), RatingRepository {

    /* ───── Network‑Cache skeleton ───── */

    override fun queryLocal()              = dao.observeAll()

    override suspend fun fetchRemote()     = firestore.getAllRatings()

    override suspend fun saveRemote(remote: List<RatingDto>) {
        dao.upsertAll(remote.map(mapper::toEntity))
    }

    override fun map(local: RatingEntity)  = mapper.toDomain(local)

    /* ───── Public API (לפי Lesson) ───── */

    override fun observeRatings(lessonId: String) =
        dao.observeForLesson(lessonId).map { list ->
            val sum   = list.sumOf { it.numericValue }
            val count = list.size
            Result.Success(sum to count)
        }

    override suspend fun getMyRating(
        lessonId: String,
        uid: String
    ): Result<Rating?> = withContext(io) {
        dao.getMyRating(lessonId, uid)
            ?.let { Result.Success(mapper.toDomain(it)) }
            ?:    Result.Success(null)
    }

    override suspend fun rateLesson(
        lessonId: String,
        numericValue: Int,
        comment: String?
    ): Result<Unit> = try {
        functions.rateLesson(lessonId, numericValue, comment)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(e)
    }
}

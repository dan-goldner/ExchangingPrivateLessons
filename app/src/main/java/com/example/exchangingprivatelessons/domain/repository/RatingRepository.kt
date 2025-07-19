package com.example.exchangingprivatelessons.domain.repository

import com.example.exchangingprivatelessons.domain.model.Rating
import kotlinx.coroutines.flow.Flow
import com.example.exchangingprivatelessons.common.util.Result

interface RatingRepository {

    /** Live rating stats (sum + count) for a lesson. */
    fun observeRatings(lessonId: String): Flow<Result<Pair<Int /*sum*/, Int /*count*/>>>

    suspend fun getMyRating(lessonId: String, uid: String): Result<Rating?>

    /** Create / update / delete (by sending same value) the caller’s rating. */
    suspend fun rateLesson(
        lessonId: String,
        numericValue: Int,
        comment: String? = null
    ): Result<Unit>
}

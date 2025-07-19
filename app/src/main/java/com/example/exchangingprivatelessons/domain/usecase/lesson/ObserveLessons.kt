// domain/usecase/lesson/ObserveLessons.kt
package com.example.exchangingprivatelessons.domain.usecase.lesson

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.model.*
import com.example.exchangingprivatelessons.domain.repository.LessonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// domain/usecase/lesson/ObserveLessons.kt
@Singleton
class ObserveLessons @Inject constructor(
    private val repo: LessonRepository
) {
    operator fun invoke(
        onlyMine: Boolean = false
    ): Flow<Result<List<ViewLesson>>> =
        repo.observeLessons(onlyMine).map { res ->
            when (res) {
                is Result.Success -> Result.Success(res.data.map { l -> mapToView(l, onlyMine) })
                is Result.Failure -> Result.Failure(res.throwable)
                is Result.Loading -> Result.Loading
            }
        }

    private fun mapToView(l: Lesson, mine: Boolean) = ViewLesson(
        lesson          = l,
        average         = l.avgRating,
        myRequestStatus = null,
        isTaken         = false,
        isMine          = mine,

        id              = l.id,
        title           = l.title,
        description     = l.description,
        ownerId         = l.ownerId,
        ownerName       = "",
        imageUrl        = null,
        createdAt       = l.createdAt,
        ratingAvg       = l.avgRating.toDouble(),
        ratingCount     = l.ratingCount,

        canEdit         = mine,
        canRequest      = !mine,
        canRate         = false,

        archived        = l.status == LessonStatus.Archived,
        canArchive      = mine
    )
}

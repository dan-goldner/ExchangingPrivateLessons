package com.example.exchangingprivatelessons.domain.usecase.lesson

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.common.util.mapList
import com.example.exchangingprivatelessons.domain.model.LessonStatus
import com.example.exchangingprivatelessons.domain.model.ViewLesson
import com.example.exchangingprivatelessons.domain.repository.TakenLessonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveTakenLessons @Inject constructor(
    private val repo: TakenLessonRepository
) {

    operator fun invoke(): Flow<Result<List<ViewLesson>>> =
        repo.observeTakenLessons().map { res ->
            when (res) {
                is Result.Success -> {
                    val mapped = res.data.mapNotNull { tk ->
                        val l = tk.lesson

                        // ❌ Skip archived lessons
                        if (l.status == LessonStatus.Archived) return@mapNotNull null

                        ViewLesson(
                            lesson          = l,
                            average         = l.avgRating,
                            myRequestStatus = null,
                            isTaken         = true,
                            isMine          = false,

                            /* ---------- flattened ---------- */
                            id              = l.id,
                            title           = l.title,
                            description     = l.description,
                            ownerId         = l.ownerId,
                            ownerName       = tk.ownerName,
                            ownerPhotoUrl   = tk.ownerPhotoUrl,
                            createdAt       = l.createdAt,
                            ratingAvg       = l.avgRating.toDouble(),
                            ratingCount     = l.ratingCount,

                            /* ---------- permissions ---------- */
                            canEdit         = false,
                            canRequest      = false,
                            canRate         = tk.canRate,

                            /* ---------- archive ---------- */
                            archived        = false,
                            canArchive      = false
                        )
                    }

                    Result.Success(mapped)
                }

                is Result.Failure -> res

                is Result.Loading -> Result.Loading
            }
        }
}
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
            res.mapList { tk ->
                val l = tk.lesson
                ViewLesson(
                    lesson          = l,
                    average         = l.avgRating,
                    myRequestStatus = null,
                    isTaken         = true,
                    isMine          = false,

                    /* ---------- flattened ---------- */
                    id           = l.id,
                    title        = l.title,
                    description  = l.description,
                    ownerId      = l.ownerId,
                    ownerName    = tk.ownerName,
                    imageUrl     = tk.ownerPhotoUrl,
                    createdAt    = l.createdAt,
                    ratingAvg    = l.avgRating.toDouble(),
                    ratingCount  = l.ratingCount,

                    /* ---------- permissions ---------- */
                    canEdit      = false,
                    canRequest   = false,
                    canRate      = tk.canRate,

                    /* ---------- archive ---------- */
                    archived     = l.status == LessonStatus.Archived,
                    canArchive   = false
                )
            }
        }
}

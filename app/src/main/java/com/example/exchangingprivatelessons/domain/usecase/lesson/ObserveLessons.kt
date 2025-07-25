package com.example.exchangingprivatelessons.domain.usecase.lesson

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.model.*
import com.example.exchangingprivatelessons.domain.repository.LessonRepository
import com.example.exchangingprivatelessons.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

class ObserveLessons @Inject constructor(
    private val lessonRepo: LessonRepository,
    private val userRepo:   UserRepository
) {

    /**
     * @param onlyMine         האם להחזיר רק שיעורים שאני הבעלים שלהם
     * @param includeArchived  האם לכלול שיעורים עם ‎status = Archived‎
     */
    /* domain/usecase/lesson/ObserveLessons.kt */
    operator fun invoke(
        onlyMine: Boolean = false
    ): Flow<Result<List<ViewLesson>>> =
        lessonRepo.observeLessons(onlyMine)
            .map { res ->
                when (res) {
                    is Result.Success -> {

                        /* ── 1. סינון לפי מצב ─────────────────────────── */
                        val lessons = if (onlyMine) {
                            /*  אני בעל‑השיעור →  כל השיעורים, גם Archived  */
                            res.data
                        } else {
                            /*  שיעורים של אחרים →  רק Active  */
                            res.data.filter { it.status == LessonStatus.Active }
                        }

                        /* ── 2. העשרת נתוני הבעלים ───────────────────── */
                        val ownerIds = lessons.map { it.ownerId }.distinct()
                        val users = (userRepo.getUsers(ownerIds) as? Result.Success)
                            ?.data.orEmpty()
                            .associateBy { it.uid }

                        Result.Success(
                            lessons.map { l -> mapToView(l, onlyMine, users[l.ownerId]) }
                        )
                    }

                    is Result.Failure -> Result.Failure(res.throwable)
                    Result.Loading    -> Result.Loading
                }
            }


    /* unchanged */
    private fun mapToView(l: Lesson, mine: Boolean, owner: User?) = ViewLesson(
        lesson          = l,
        average         = l.avgRating,
        myRequestStatus = null,
        isTaken         = false,
        isMine          = mine,

        id              = l.id,
        title           = l.title,
        description     = l.description,
        ownerId         = l.ownerId,
        ownerName       = owner?.displayName ?: "Unknown",
        ownerPhotoUrl   = owner?.photoUrl    ?: "",
        createdAt       = l.createdAt,
        ratingAvg       = l.avgRating.toDouble(),
        ratingCount     = l.ratingCount,

        canEdit     = mine,
        canRequest  = !mine,
        canRate     = false,

        archived    = l.status == LessonStatus.Archived,
        canArchive  = mine
    )
}

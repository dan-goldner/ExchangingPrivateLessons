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

@Singleton
class ObserveLessons @Inject constructor(
    private val lessonRepo: LessonRepository,
    private val userRepo: UserRepository
) {
    operator fun invoke(
        onlyMine: Boolean = false
    ): Flow<Result<List<ViewLesson>>> =
        lessonRepo.observeLessons(onlyMine).map { res ->
            when (res) {
                is Result.Success -> {
                    val lessons = res.data
                    val ownerIds = lessons.map { it.ownerId }.distinct()

                    val usersResult = runCatching {
                        userRepo.getUsers(ownerIds)
                    }.getOrNull()

                    val users = (usersResult as? Result.Success)?.data.orEmpty()
                    val ownerMap = users.associateBy { it.uid }

                    Result.Success(lessons.map { l ->
                        val owner = ownerMap[l.ownerId]
                        mapToView(l, onlyMine, owner)
                    })
                }

                is Result.Failure -> Result.Failure(res.throwable)
                is Result.Loading -> Result.Loading
            }
        }

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
        ownerPhotoUrl   = owner?.photoUrl ?: "",
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

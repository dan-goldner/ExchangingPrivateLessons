package com.example.exchangingprivatelessons.domain.usecase.lesson

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.model.LessonStatus
import com.example.exchangingprivatelessons.domain.model.ViewLesson
import com.example.exchangingprivatelessons.domain.model.RequestStatus
import com.example.exchangingprivatelessons.domain.repository.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetLessonDetails @Inject constructor(
    private val lessonRepo:   LessonRepository,
    private val requestRepo:  LessonRequestRepository,
    private val ratingRepo:   RatingRepository,
    private val userRepo:     UserRepository
) {

    suspend operator fun invoke(lessonId: String): Result<ViewLesson> {

        val lessonRes = lessonRepo.getLesson(lessonId)
        when (lessonRes) {
            is Result.Failure -> return Result.Failure(lessonRes.throwable)
            is Result.Loading -> return Result.Loading
            is Result.Success -> { /* ממשיכים */ }
        }

        val lesson = lessonRes.data
        val meUid = userRepo.currentUid()

        val owner = (userRepo.getUser(lesson.ownerId) as? Result.Success)?.data
        val myRequest = meUid?.let {
            (requestRepo.getMyRequest(lessonId, it) as? Result.Success)?.data
        }
        val myRating = meUid?.let {
            (ratingRepo.getMyRating(lessonId, it) as? Result.Success)?.data
        }

        val isMine = lesson.ownerId == meUid
        val isTaken = myRequest?.status == RequestStatus.Approved
        val canRequest = !isMine && myRequest == null
        val canRate = !isMine && myRating == null
        val canEdit = isMine

        val average = if (lesson.ratingCount > 0)
            lesson.ratingSum.toFloat() / lesson.ratingCount
        else 0f

        val view = ViewLesson(
            lesson          = lesson,
            average         = average,
            myRequestStatus = myRequest?.status,
            isTaken         = isTaken,
            isMine          = isMine,
            id              = lesson.id,
            title           = lesson.title,
            description     = lesson.description,
            ownerId         = lesson.ownerId,
            ownerName       = owner?.displayName ?: "Unknown",
            imageUrl        = owner?.photoUrl,
            createdAt       = lesson.createdAt,
            ratingAvg       = average.toDouble(),
            ratingCount     = lesson.ratingCount,
            canEdit         = canEdit,
            canRequest      = canRequest,
            canRate         = canRate,
            archived        = lesson.status == LessonStatus.Archived,
            canArchive      = isMine
        )


        return Result.Success(view)
    }
}

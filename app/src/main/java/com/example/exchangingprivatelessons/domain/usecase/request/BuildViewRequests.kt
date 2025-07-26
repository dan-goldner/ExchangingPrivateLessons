package com.example.exchangingprivatelessons.domain.usecase.request

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.model.*
import com.example.exchangingprivatelessons.domain.repository.*
import com.example.exchangingprivatelessons.ui.request.RequestsViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuildViewRequests @Inject constructor(
    private val reqRepo : LessonRequestRepository,
    private val lessonRepo: LessonRepository,
    private val userRepo  : UserRepository
) {

    /** זרם ‎ViewRequestItem לפי mode + status (‑status רלוונטי רק SENT) */
    operator fun invoke(
        mode  : RequestsViewModel.Mode,
        status: RequestStatus?
    ): Flow<Result<List<ViewRequestItem>>> =
        when (mode) {
            RequestsViewModel.Mode.RECEIVED ->
                reqRepo.observeIncomingRequests()
                    .enrich(mode) { it }            // בלי סינון נוסף

            RequestsViewModel.Mode.SENT     ->
                reqRepo.observeRequestsByStatus(
                    reqRepo.currentUid() ?: "",
                    status?.name                    // null ⇒ כל הסטטוסים
                ).enrich(mode) { it }
        }

    /* ---------- helper ---------- */
    private fun Flow<Result<List<LessonRequest>>>.enrich(
        viewMode: RequestsViewModel.Mode,
        filter  : (List<LessonRequest>) -> List<LessonRequest>
    ): Flow<Result<List<ViewRequestItem>>> =
        flatMapLatest { res ->
            if (res !is Result.Success)
                return@flatMapLatest flowOf(res as Result<List<ViewRequestItem>>)

            val list = filter(res.data).distinctBy { it.id }

            /* --- שליפת נתונים מרוכזת --- */
            val lessonIds = list.map { it.lessonId }.distinct()
            val userIds   = list.flatMap { listOf(it.ownerId, it.requesterId) }.distinct()

            val lessons = lessonIds.associateWith { id ->
                (lessonRepo.getLesson(id) as? Result.Success)?.data
            }
            val users = userIds.associateWith { uid ->
                (userRepo.getUser(uid) as? Result.Success)?.data
            }

            val myUid = userRepo.currentUid() ?: ""

            val items = list.map { req ->
                val owner     = users[req.ownerId]
                val requester = users[req.requesterId]

                val canRespond = viewMode == RequestsViewModel.Mode.RECEIVED &&
                        req.ownerId  == myUid &&
                        req.status   == RequestStatus.Pending

                val canCancel  = viewMode == RequestsViewModel.Mode.SENT &&
                        req.requesterId == myUid &&
                        req.status      == RequestStatus.Pending

                ViewRequestItem(
                    id                = req.id,
                    lessonTitle       = lessons[req.lessonId]?.title ?: "—",
                    ownerName         = owner?.displayName     ?: "—",
                    ownerPhotoUrl     = owner?.photoUrl,
                    requesterName     = requester?.displayName ?: "—",
                    requesterPhotoUrl = requester?.photoUrl,
                    requestedAt       = req.requestedAt,
                    respondedAt       = req.respondedAt,
                    status            = req.status,
                    canRespond        = canRespond,
                    canCancel         = canCancel,
                    viewMode          = viewMode
                )
            }

            flowOf(Result.Success(items))
        }
}

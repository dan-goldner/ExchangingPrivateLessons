package com.example.exchangingprivatelessons.domain.usecase.request

import com.example.exchangingprivatelessons.domain.model.LessonRequest
import com.example.exchangingprivatelessons.domain.model.RequestStatus
import com.example.exchangingprivatelessons.domain.repository.LessonRepository
import com.example.exchangingprivatelessons.domain.repository.LessonRequestRepository
import com.example.exchangingprivatelessons.domain.repository.UserRepository
import com.example.exchangingprivatelessons.ui.request.RequestsViewModel
import com.example.exchangingprivatelessons.ui.request.ViewRequestItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import com.example.exchangingprivatelessons.common.util.Result
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuildViewRequests @Inject constructor(
    private val reqRepo : LessonRequestRepository,
    private val lessonRepo: LessonRepository,
    private val userRepo  : UserRepository
) {

    /** stream של ViewRequestItem עפ״י mode+status */
    operator fun invoke(
        mode: RequestsViewModel.Mode,
        status: RequestStatus?
    ): Flow<Result<List<ViewRequestItem>>> = when (mode) {
        RequestsViewModel.Mode.RECEIVED ->
            reqRepo.observeIncomingRequests().enrich { it }      // אין סינון סטטוס
        RequestsViewModel.Mode.SENT ->
            reqRepo.observeRequestsByStatus(
                reqRepo.currentUid() ?: "", status!!.name
            ).enrich { it.filter { r -> r.status == status } }
    }


    private fun Flow<Result<List<LessonRequest>>>.enrich(
        filter: (List<LessonRequest>) -> List<LessonRequest>
    ): Flow<Result<List<ViewRequestItem>>> =
        flatMapLatest { res ->
            if (res !is Result.Success) return@flatMapLatest flowOf(res as Result<List<ViewRequestItem>>)

            val list = filter(res.data).distinctBy { it.id }

            /* מקבץ שאילתות – ללא בלוקים N+1 */
            val lessonIds = list.map { it.lessonId }.distinct()
            val userIds = list                     // ← אוספים *גם* את בעלי השיעור וגם את המבקשים
                .flatMap { listOf(it.ownerId, it.requesterId) }
                .distinct()

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

                ViewRequestItem(
                    id               = req.id,
                    lessonTitle      = lessons[req.lessonId]?.title ?: "—",
                    ownerName      = owner?.displayName     ?: "—",
                    ownerPhotoUrl  = owner?.photoUrl,
                    requesterName    = requester?.displayName ?: "—",
                    requesterPhotoUrl= requester?.photoUrl,
                    requestedAt      = req.requestedAt,
                    status           = req.status,
                    canRespond = req.ownerId     == myUid && req.status == RequestStatus.Pending,
                    canCancel  = req.requesterId == myUid && req.status == RequestStatus.Pending
                )
            }
            flowOf(Result.Success(items))
        }
}

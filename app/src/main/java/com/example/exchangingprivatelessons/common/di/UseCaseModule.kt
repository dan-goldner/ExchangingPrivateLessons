package com.example.exchangingprivatelessons.common.di

import com.example.exchangingprivatelessons.domain.repository.*
import com.example.exchangingprivatelessons.domain.usecase.chat.*
import com.example.exchangingprivatelessons.domain.usecase.lesson.*
import com.example.exchangingprivatelessons.domain.usecase.rating.RateLesson
import com.example.exchangingprivatelessons.domain.usecase.request.*
import com.example.exchangingprivatelessons.domain.usecase.user.*
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    /* ---------- user ---------- */
    @Provides fun signInOrUp      (r: UserRepository) = SignInOrUp(r)
    @Provides fun updateProfile   (r: UserRepository) = UpdateProfile(r)
    @Provides fun observeUser     (r: UserRepository) = ObserveUser(r)
    @Provides fun deleteAccount   (r: UserRepository) = DeleteAccount(r)

    /* ---------- chat ---------- */
    @Provides fun createChat      (c: ChatRepository)                         = CreateChat(c)
    @Provides fun observeAllChats (c: ChatRepository)                         = ObserveAllChats(c)
    @Provides fun observeChatMsgs (c: ChatRepository)                         = ObserveChatMessages(c)
    @Provides fun refreshAllChats (c: ChatRepository)                         = RefreshAllChats(c)
    @Provides fun sendMessage     (c: ChatRepository, cc: CreateChat)         = SendMessage(c, cc)

    /* ---------- lesson ---------- */
    @Provides fun createLesson    (l: LessonRepository)                       = CreateLesson(l)
    @Provides fun updateLesson    (l: LessonRepository)                       = UpdateLesson(l)
    @Provides fun observeLessons  (l: LessonRepository)                       = ObserveLessons(l)
    @Provides fun observeTaken    (t: TakenLessonRepository)                  = ObserveTakenLessons(t)
    @Provides fun refreshLessons  (l: LessonRepository, a: FirebaseAuth)      = RefreshLessons(l, a)
    @Provides fun archiveLesson   (l: LessonRepository)                       = ArchiveLesson(l)
    @Provides
    fun getLessonDetails(
        l: LessonRepository,
        r: LessonRequestRepository,
        ra: RatingRepository,
        u: UserRepository
    ) = GetLessonDetails(l, r, ra, u)

    /* ---------- request ---------- */
    @Provides fun requestLesson   (r: LessonRequestRepository)                = RequestLesson(r)
    @Provides fun observeIncoming (r: LessonRequestRepository)                = ObserveIncomingRequests(r)
    @Provides fun refreshRequests (r: LessonRequestRepository)                = RefreshLessonRequests(r)
    @Provides fun observeByStatus (r: LessonRequestRepository)                = ObserveRequestsByStatus(r)
    @Provides fun approveRequest  (r: LessonRequestRepository)                = ApproveRequest(r)
    @Provides fun declineRequest  (r: LessonRequestRepository)                = DeclineRequest(r)
    @Provides fun cancelRequest   (r: LessonRequestRepository)                = CancelRequest(r)

    /* ---------- rating ---------- */
    @Provides fun rateLesson      (r: RatingRepository)                       = RateLesson(r)
}

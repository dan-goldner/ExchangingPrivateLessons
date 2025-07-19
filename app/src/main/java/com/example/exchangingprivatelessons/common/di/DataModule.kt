package com.example.exchangingprivatelessons.common.di

import android.content.Context
import androidx.room.Room
import com.example.exchangingprivatelessons.data.local.AppDatabase
import com.example.exchangingprivatelessons.data.local.dao.*
import com.example.exchangingprivatelessons.data.mapper.*
import com.example.exchangingprivatelessons.data.remote.cloud.FunctionsDataSource
import com.example.exchangingprivatelessons.data.remote.cloud.StorageDataSource
import com.example.exchangingprivatelessons.data.remote.firestore.FirestoreDataSource
import com.example.exchangingprivatelessons.data.repository.*
import com.example.exchangingprivatelessons.domain.repository.*
import com.example.exchangingprivatelessons.domain.usecase.chat.*
import com.example.exchangingprivatelessons.domain.usecase.lesson.*
import com.example.exchangingprivatelessons.domain.usecase.rating.RateLesson
import com.example.exchangingprivatelessons.domain.usecase.request.*
import com.example.exchangingprivatelessons.domain.usecase.user.*
import com.google.firebase.app
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app

import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.mapstruct.factory.Mappers
import javax.inject.Qualifier
import javax.inject.Singleton

/* ── Qualifier ─────────────────────────────────────────────────────────── */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/* ── Main DI‑module ─────────────────────────────────────────────────────── */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    /* ---------- Dispatchers ---------- */

    @Provides @IoDispatcher
    fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO


    /* ---------- Firebase singletons ---------- */

    @Provides @Singleton fun firestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    @Provides @Singleton fun auth():       FirebaseAuth      = FirebaseAuth.getInstance()

    /** Callable‑Functions – אנו עובדים באיזור `me-west1` בלבד */
    @Provides @Singleton
    fun functions(): FirebaseFunctions =
        FirebaseFunctions.getInstance(Firebase.app /* default */, "me-west1")

    @Provides @Singleton fun storage(): FirebaseStorage = FirebaseStorage.getInstance()


    /* ---------- Data‑sources ---------- */

    @Provides @Singleton
    fun firestoreDS(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ) = FirestoreDataSource(firestore, auth)

    @Provides @Singleton
    fun functionsDS(fn: FirebaseFunctions) = FunctionsDataSource(fn)

    @Provides @Singleton
    fun storageDS(store: FirebaseStorage)  = StorageDataSource(store)


    /* ---------- Room database ---------- */

    @Provides @Singleton
    fun db(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "app.db")
            .fallbackToDestructiveMigration()
            .build()

    /* Dao‑ים – אין צורך ב‑Singleton (מקבל מה‑DB) */
    @Provides fun chatDao(db: AppDatabase)        = db.chatDao()
    @Provides fun lessonDao(db: AppDatabase)      = db.lessonDao()
    @Provides fun requestDao(db: AppDatabase)     = db.lessonRequestDao()
    @Provides fun messageDao(db: AppDatabase)     = db.messageDao()
    @Provides fun ratingDao(db: AppDatabase)      = db.ratingDao()
    @Provides fun takenLessonDao(db: AppDatabase) = db.takenLessonDao()
    @Provides fun userDao(db: AppDatabase)        = db.userDao()


    /* ---------- MapStruct mappers ---------- */
    @Provides @Singleton fun lessonMapper():         LessonMapper         = Mappers.getMapper(LessonMapper::class.java)
    @Provides @Singleton fun chatMapper():           ChatMapper           = Mappers.getMapper(ChatMapper::class.java)
    @Provides @Singleton fun requestMapper():        LessonRequestMapper  = Mappers.getMapper(LessonRequestMapper::class.java)
    @Provides @Singleton fun ratingMapper():         RatingMapper         = Mappers.getMapper(RatingMapper::class.java)
    @Provides @Singleton fun takenLessonMapper():    TakenLessonMapper    = Mappers.getMapper(TakenLessonMapper::class.java)
    @Provides @Singleton fun userMapper():           UserMapper           = Mappers.getMapper(UserMapper::class.java)
    @Provides @Singleton fun messageMapper():        MessageMapper        = Mappers.getMapper(MessageMapper::class.java)


    /* ---------- Repositories ---------- */
    @Provides @Singleton fun chatRepo(impl: ChatRepositoryImpl):                     ChatRepository          = impl
    @Provides @Singleton fun lessonRepo(impl: LessonRepositoryImpl):                 LessonRepository        = impl
    @Provides @Singleton fun reqRepo(impl: LessonRequestRepositoryImpl):             LessonRequestRepository = impl
    /* ---------- Repositories ---------- */
    @Provides @Singleton
    fun ratingRepo(impl: RatingRepositoryImpl): RatingRepository = impl
    @Provides @Singleton fun takenRepo(impl: TakenLessonRepositoryImpl):             TakenLessonRepository   = impl
    @Provides @Singleton fun userRepo(impl: UserRepositoryImpl):                     UserRepository          = impl


    /* ---------- Use‑cases (facade) ---------- */
    @Module
    @InstallIn(SingletonComponent::class)
    object UseCaseModule {

        /* user */
        @Provides fun signInOrUp(repo: UserRepository)       = SignInOrUp(repo)
        @Provides fun updateProfile(repo: UserRepository)    = UpdateProfile(repo)
        @Provides fun observeUser(repo: UserRepository)  = ObserveUser(repo)
        @Provides fun deleteAccount(repo: UserRepository) = DeleteAccount(repo)

        /* chat */
        @Provides fun createChat(repo: ChatRepository)                   = CreateChat(repo)
        @Provides fun observeAllChats(repo: ChatRepository)              = ObserveAllChats(repo)
        @Provides fun observeChatMessages(repo: ChatRepository)          = ObserveChatMessages(repo)
        @Provides fun refreshAllChats(repo: ChatRepository)              = RefreshAllChats(repo)
        @Provides fun sendMessage(repo: ChatRepository, c: CreateChat)   = SendMessage(repo, c)

        /* lesson */
        @Provides fun createLesson(repo: LessonRepository)               = CreateLesson(repo)
        @Provides fun updateLesson(repo: LessonRepository)               = UpdateLesson(repo)
        @Provides fun observeLessons(repo: LessonRepository)             = ObserveLessons(repo)
        @Provides fun observeTaken(repo: TakenLessonRepository)          = ObserveTakenLessons(repo)
        @Provides fun refreshLessons(repo: LessonRepository)             = RefreshLessons(repo)
        @Provides fun archiveLesson(repo: LessonRepository)              = ArchiveLesson(repo)
        @Provides fun getLessonDetails(
            l: LessonRepository, r: LessonRequestRepository,
            ra: RatingRepository, u: UserRepository
        ) = GetLessonDetails(l, r, ra, u)

        /* request */
        @Provides fun requestLesson(repo: LessonRequestRepository)       = RequestLesson(repo)
        @Provides fun observeIncoming(repo: LessonRequestRepository)     = ObserveIncomingRequests(repo)
        @Provides fun refreshRequests(repo: LessonRequestRepository)     = RefreshLessonRequests(repo)
        @Provides fun observeByStatus(repo: LessonRequestRepository)   = ObserveRequestsByStatus(repo)
        @Provides fun approveRequest(repo: LessonRequestRepository)    = ApproveRequest(repo)
        @Provides fun declineRequest(repo: LessonRequestRepository)    = DeclineRequest(repo)
        @Provides fun cancelRequest(repo: LessonRequestRepository)     = CancelRequest(repo)

        /* rating */
        @Provides fun rateLesson(repo: RatingRepository)                 = RateLesson(repo)
    }
}

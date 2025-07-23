package com.example.exchangingprivatelessons.common.di

import android.content.Context
import androidx.room.Room
import com.example.exchangingprivatelessons.data.local.AppDatabase
import com.example.exchangingprivatelessons.data.local.dao.*
import com.example.exchangingprivatelessons.data.mapper.*
import com.example.exchangingprivatelessons.data.remote.cloud.*
import com.example.exchangingprivatelessons.data.remote.firestore.FirestoreDataSource
import com.example.exchangingprivatelessons.data.repository.*
import com.example.exchangingprivatelessons.domain.repository.*
import com.example.exchangingprivatelessons.domain.usecase.chat.*
import com.example.exchangingprivatelessons.domain.usecase.lesson.*
import com.example.exchangingprivatelessons.domain.usecase.rating.RateLesson
import com.example.exchangingprivatelessons.domain.usecase.request.*
import com.example.exchangingprivatelessons.domain.usecase.user.*
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
import kotlinx.coroutines.*
import org.mapstruct.factory.Mappers
import javax.inject.Qualifier
import javax.inject.Singleton

/* ───── Qualifiers ───── */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    /* ---------- Coroutine Dispatchers / Scope ---------- */

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    @ApplicationScope
    fun provideAppScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /* ---------- Room DB + DAO‑ים ---------- */

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "app.db")
            .addMigrations(
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4
            )
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun chatDao(db: AppDatabase): ChatDao               = db.chatDao()
    @Provides fun lessonDao(db: AppDatabase): LessonDao           = db.lessonDao()
    @Provides fun requestDao(db: AppDatabase): LessonRequestDao   = db.lessonRequestDao()
    @Provides fun messageDao(db: AppDatabase): MessageDao         = db.messageDao()
    @Provides fun ratingDao(db: AppDatabase): RatingDao           = db.ratingDao()
    @Provides fun takenLessonDao(db: AppDatabase): TakenLessonDao = db.takenLessonDao()
    @Provides fun userDao(db: AppDatabase): UserDao               = db.userDao()

    /* ---------- Firebase Singletons ---------- */

    @Provides @Singleton fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    @Provides @Singleton fun provideAuth()     : FirebaseAuth      = FirebaseAuth.getInstance()
    @Provides @Singleton
    fun provideFunctions(): FirebaseFunctions =
        FirebaseFunctions.getInstance(Firebase.app /* default */, "me-west1")
    @Provides @Singleton fun provideStorage(): FirebaseStorage     = FirebaseStorage.getInstance()

    /* ---------- Data‑Sources ---------- */

    @Provides @Singleton
    fun firestoreDS(fs: FirebaseFirestore, auth: FirebaseAuth) =
        FirestoreDataSource(fs, auth)

    @Provides @Singleton fun functionsDS(fn: FirebaseFunctions)   = FunctionsDataSource(fn)
    @Provides @Singleton fun storageDS  (st: FirebaseStorage)     = StorageDataSource(st)

    /* ---------- MapStruct Mappers ---------- */

    @Provides @Singleton fun lessonMapper()  : LessonMapper        = Mappers.getMapper(LessonMapper::class.java)
    @Provides @Singleton fun chatMapper()    : ChatMapper          = Mappers.getMapper(ChatMapper::class.java)
    @Provides @Singleton fun requestMapper() : LessonRequestMapper = Mappers.getMapper(LessonRequestMapper::class.java)
    @Provides @Singleton fun ratingMapper()  : RatingMapper        = Mappers.getMapper(RatingMapper::class.java)
    @Provides @Singleton fun takenMapper()   : TakenLessonMapper   = Mappers.getMapper(TakenLessonMapper::class.java)
    @Provides @Singleton fun userMapper()    : UserMapper          = Mappers.getMapper(UserMapper::class.java)
    @Provides @Singleton fun messageMapper() : MessageMapper       = Mappers.getMapper(MessageMapper::class.java)

    /* ---------- Repositories ---------- */

    @Provides @Singleton fun chatRepo   (impl: ChatRepositoryImpl)          : ChatRepository          = impl
    @Provides @Singleton fun lessonRepo (impl: LessonRepositoryImpl)        : LessonRepository        = impl
    @Provides @Singleton fun reqRepo    (impl: LessonRequestRepositoryImpl) : LessonRequestRepository = impl
    @Provides @Singleton fun ratingRepo (impl: RatingRepositoryImpl)        : RatingRepository        = impl
    @Provides @Singleton fun takenRepo  (impl: TakenLessonRepositoryImpl)   : TakenLessonRepository   = impl
    @Provides @Singleton fun userRepo   (impl: UserRepositoryImpl)          : UserRepository          = impl
}

/* ───────────────────────────────────────────────────────── */
/* Use‑cases  –  שמנו במודול נפרד כדי לא לנפח את DataModule  */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    /* ---------- user ---------- */
    @Provides fun signInOrUp   (u: UserRepository)   = SignInOrUp(u)
    @Provides fun updateProfile(u: UserRepository)   = UpdateProfile(u)
    @Provides fun observeUser (u: UserRepository)    = ObserveUser(u)
    @Provides fun deleteAccount(u: UserRepository)   = DeleteAccount(u)

    /* ---------- chat ---------- */
    @Provides fun createChat       (c: ChatRepository)                         = CreateChat(c)
    @Provides fun observeAllChats  (c: ChatRepository)                         = ObserveAllChats(c)
    @Provides fun observeChatMsgs  (c: ChatRepository)                         = ObserveChatMessages(c)
    @Provides fun refreshAllChats  (c: ChatRepository)                         = RefreshAllChats(c)
    @Provides fun sendMessage      (c: ChatRepository, cc: CreateChat)         = SendMessage(c, cc)

    /* ---------- lesson ---------- */
    @Provides fun createLesson (l: LessonRepository)                           = CreateLesson(l)
    @Provides fun updateLesson (l: LessonRepository)                           = UpdateLesson(l)
    @Provides fun observeLessons(l: LessonRepository)                          = ObserveLessons(l)
    @Provides fun observeTaken  (t: TakenLessonRepository)                     = ObserveTakenLessons(t)
    @Provides fun refreshLessons(l: LessonRepository, auth: FirebaseAuth)      = RefreshLessons(l, auth)
    @Provides fun archiveLesson (l: LessonRepository)                          = ArchiveLesson(l)
    @Provides
    fun getLessonDetails(
        l: LessonRepository,
        r: LessonRequestRepository,
        ra: RatingRepository,
        u: UserRepository
    ) = GetLessonDetails(l, r, ra, u)

    /* ---------- request ---------- */
    @Provides fun requestLesson   (r: LessonRequestRepository)                 = RequestLesson(r)
    @Provides fun observeIncoming (r: LessonRequestRepository)                 = ObserveIncomingRequests(r)
    @Provides fun refreshRequests (r: LessonRequestRepository)                 = RefreshLessonRequests(r)
    @Provides fun observeByStatus (r: LessonRequestRepository)                 = ObserveRequestsByStatus(r)
    @Provides fun approveRequest  (r: LessonRequestRepository)                 = ApproveRequest(r)
    @Provides fun declineRequest  (r: LessonRequestRepository)                 = DeclineRequest(r)
    @Provides fun cancelRequest   (r: LessonRequestRepository)                 = CancelRequest(r)

    /* ---------- rating ---------- */
    @Provides fun rateLesson(r: RatingRepository)                              = RateLesson(r)
}

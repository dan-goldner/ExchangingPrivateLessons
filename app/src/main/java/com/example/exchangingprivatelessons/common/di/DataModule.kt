package com.example.exchangingprivatelessons.common.di

import android.content.Context
import androidx.room.Room
import com.example.exchangingprivatelessons.data.local.AppDatabase
import com.example.exchangingprivatelessons.data.local.dao.*
import com.example.exchangingprivatelessons.data.mapper.*
import com.example.exchangingprivatelessons.data.remote.cloud.*
import com.example.exchangingprivatelessons.data.remote.firestore.FirestoreDataSource
import com.example.exchangingprivatelessons.data.remote.storage.StorageDataSource
import com.example.exchangingprivatelessons.data.repository.*
import com.example.exchangingprivatelessons.domain.repository.*
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

/* ───────── Qualifiers ───────── */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/* ───────── Dagger Module ───────── */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    /* ---------- Coroutines ---------- */
    @Provides @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides @Singleton @ApplicationScope
    fun provideAppScope(@IoDispatcher io: CoroutineDispatcher): CoroutineScope =
        CoroutineScope(SupervisorJob() + io)

    /* ---------- Room ---------- */
    @Provides @Singleton
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

    /* ---------- Firebase singletons ---------- */
    @Provides @Singleton fun firestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    @Provides @Singleton fun auth():      FirebaseAuth      = FirebaseAuth.getInstance()
    @Provides @Singleton fun functions(): FirebaseFunctions =
        FirebaseFunctions.getInstance(Firebase.app, "me-west1")
    @Provides @Singleton fun storage():   FirebaseStorage   = FirebaseStorage.getInstance()

    /* ---------- Data‑sources ---------- */
    @Provides @Singleton
    fun firestoreDS(fs: FirebaseFirestore, a: FirebaseAuth) =
        FirestoreDataSource(fs, a)

    @Provides @Singleton fun functionsDS(fn: FirebaseFunctions) = FunctionsDataSource(fn)
    @Provides @Singleton fun storageDS  (st: FirebaseStorage)  = StorageDataSource(st)

    /* ---------- MapStruct mappers ---------- */
    @Provides @Singleton fun lessonMapper()  : LessonMapper        = Mappers.getMapper(LessonMapper::class.java)
    @Provides @Singleton fun chatMapper()    : ChatMapper          = Mappers.getMapper(ChatMapper::class.java)
    @Provides @Singleton fun requestMapper() : LessonRequestMapper = Mappers.getMapper(LessonRequestMapper::class.java)
    @Provides @Singleton fun ratingMapper()  : RatingMapper        = Mappers.getMapper(RatingMapper::class.java)
    @Provides @Singleton fun takenMapper()   : TakenLessonMapper   = Mappers.getMapper(TakenLessonMapper::class.java)
    @Provides @Singleton fun userMapper()    : UserMapper          = Mappers.getMapper(UserMapper::class.java)
    @Provides @Singleton fun messageMapper() : MessageMapper       = Mappers.getMapper(MessageMapper::class.java)

    /* ---------- Repositories ---------- */
    @Provides @Singleton fun chatRepo  (impl: ChatRepositoryImpl)          : ChatRepository          = impl
    @Provides @Singleton fun lessonRepo(impl: LessonRepositoryImpl)        : LessonRepository        = impl
    @Provides @Singleton fun reqRepo   (impl: LessonRequestRepositoryImpl) : LessonRequestRepository = impl
    @Provides @Singleton fun ratingRepo(impl: RatingRepositoryImpl)        : RatingRepository        = impl
    @Provides @Singleton fun takenRepo (impl: TakenLessonRepositoryImpl)   : TakenLessonRepository   = impl
    @Provides @Singleton fun userRepo  (impl: UserRepositoryImpl)          : UserRepository          = impl
}

package com.example.exchangingprivatelessons.data.repository

import com.example.exchangingprivatelessons.common.di.IoDispatcher
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.data.local.dao.UserDao
import com.example.exchangingprivatelessons.data.local.entity.UserEntity
import com.example.exchangingprivatelessons.data.mapper.UserMapper
import com.example.exchangingprivatelessons.data.remote.cloud.FunctionsDataSource
import com.example.exchangingprivatelessons.data.remote.dto.UserDto
import com.example.exchangingprivatelessons.data.remote.firestore.FirestoreDataSource
import com.example.exchangingprivatelessons.data.repository.base.NetworkCacheRepository
import com.example.exchangingprivatelessons.domain.model.User
import com.example.exchangingprivatelessons.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore:  FirestoreDataSource,
    private val functions:  FunctionsDataSource,   // ←  **ככה!**
    private val dao:        UserDao,
    private val mapper:     UserMapper,
    private val auth:       FirebaseAuth,
    @IoDispatcher
    private val io:         CoroutineDispatcher
) : NetworkCacheRepository<UserEntity, UserDto, User>(io), UserRepository {

    /* ───────────── Network-Bound  (cache + server) ───────────── */

    override fun queryLocal(): Flow<List<UserEntity>> {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be logged-in before observing")
        return dao.observe(uid).map { listOfNotNull(it) }      // יחיד-ברשימה
    }

    override suspend fun fetchRemote(): List<UserDto> =
        listOf(firestore.getMe())         // תמיד משתמש יחיד

    override suspend fun saveRemote(remote: List<UserDto>) {
        remote.firstOrNull()?.let { dao.upsert(mapper.toEntity(it)) }
    }

    override fun map(local: UserEntity): User = mapper.toDomain(local)

    /* ───────────── Public API  (implements UserRepository) ───────────── */

    override fun observeUser(uid: String): Flow<Result<User>> =
        dao.observe(uid).map { entity ->
            if (entity == null)
                Result.Failure(NoSuchElementException("User $uid not found"))
            else
                Result.Success(mapper.toDomain(entity))
        }

    override fun observeMe(): Flow<Result<User>> {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be logged-in to observeMe()")

        return dao.observe(uid).map { entity ->
            if (entity == null)
                Result.Failure(NoSuchElementException("Current user not found in local DB"))
            else
                Result.Success(mapper.toDomain(entity))
        }
    }


    override suspend fun getMe(): Result<User> = withContext(io) {
        runCatching {
            val uid = auth.currentUser?.uid
                ?: throw IllegalStateException("User must be logged-in to getMe()")

            val local = dao.get(uid)
            if (local != null) mapper.toDomain(local)
            else {
                val remote = firestore.getMe()
                val entity = mapper.toEntity(remote)
                dao.upsert(entity)
                mapper.toDomain(entity)
            }
        }.fold(
            onSuccess = { Result.Success(it) },
            onFailure = { Result.Failure(it) }
        )
    }

    override suspend fun getUser(uid: String): Result<User> = withContext(io) {
        runCatching {
            val local = dao.get(uid)
            if (local != null) mapper.toDomain(local)
            else {
                val remote = firestore.getUser(uid)
                val entity = mapper.toEntity(remote)
                dao.upsert(entity)
                mapper.toDomain(entity)
            }
        }.fold(
            onSuccess = { Result.Success(it) },
            onFailure = { Result.Failure(it) }
        )
    }

    /** Registration + Login  */
    override suspend fun signInOrUpWithEmail(
        email: String,
        password: String
    ): Result<User> = withContext(io) {

        suspend fun createOrSignIn(): String /* uid */ = try {
            // ① מנסה ליצור משתמש חדש
            auth.createUserWithEmailAndPassword(email, password)
                .await().user!!.uid
        } catch (e: FirebaseAuthUserCollisionException) {
            // ② אם האימייל כבר קיים → Login
            auth.signInWithEmailAndPassword(email, password)
                .await().user!!.uid
        }

        try {
            val uid = createOrSignIn()

            val dto    = functions.signInOrUp(email, password)
            val entity = mapper.toEntity(dto)
            dao.upsert(entity)
            Result.Success(mapper.toDomain(entity))

        } catch (err: Exception) {
            val msg = when (err) {
                is FirebaseAuthInvalidCredentialsException ->
                    "סיסמה שגויה"                       // ← רק במקרה LOGIN
                is FirebaseAuthWeakPasswordException ->
                    "הסיסמה חייבת להיות בת 6 תווים לפחות"
                is FirebaseAuthInvalidUserException ->
                    "החשבון לא קיים – הירשם תחילה"
                else -> err.message ?: "שגיאה לא ידועה"
            }
            Result.Failure(Exception(msg, err))
        }
    }





    /* ---------------- helper קטנטן ---------------- */
    private suspend fun <T> retry(times: Int, delayMs: Long, block: suspend () -> T): T {
        repeat(times - 1) {
            try { return block() } catch (_: Exception) { delay(delayMs) }
        }
        return block()           // ניסיון אחרון – אם ייכשל  ➔  ייזרק החוצה
    }


    override suspend fun updateProfile(
        displayName: String?,
        bio: String?,
        photoUrl: String?
    ): Result<Unit> = withContext(io) {
        runCatching {
            functions.updateProfile(displayName, bio, photoUrl)
        }.fold(
            onSuccess = { Result.Success(Unit) },
            onFailure = { Result.Failure(it) }
        )
    }

    override suspend fun deleteMyAccount(): Result<Unit> = withContext(io) {
        runCatching { functions.deleteMyAccount() }
            .fold(
                onSuccess = { Result.Success(Unit) },
                onFailure = { Result.Failure(it) }
            )
    }

    override suspend fun touchLogin(): Result<Unit> = withContext(io) {
        runCatching { functions.touchLogin() }
            .fold(
                onSuccess = { Result.Success(Unit) },
                onFailure = { Result.Failure(it) }
            )
    }


    override fun currentUid(): String? = auth.currentUser?.uid

}

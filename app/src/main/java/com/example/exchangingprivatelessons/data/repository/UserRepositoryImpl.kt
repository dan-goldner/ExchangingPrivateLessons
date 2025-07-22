package com.example.exchangingprivatelessons.data.repository

import com.example.exchangingprivatelessons.common.di.IoDispatcher
import com.example.exchangingprivatelessons.common.di.ApplicationScope
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.common.util.map
import com.example.exchangingprivatelessons.data.local.dao.UserDao
import com.example.exchangingprivatelessons.data.local.entity.UserEntity
import com.example.exchangingprivatelessons.data.mapper.UserMapper
import com.example.exchangingprivatelessons.data.remote.cloud.FunctionsDataSource
import com.example.exchangingprivatelessons.data.remote.dto.UserDto
import com.example.exchangingprivatelessons.data.remote.firestore.FirestoreDataSource
import com.example.exchangingprivatelessons.data.repository.base.LiveSyncRepository
import com.example.exchangingprivatelessons.domain.model.User
import com.example.exchangingprivatelessons.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore : FirestoreDataSource,
    private val functions : FunctionsDataSource,
    private val dao       : UserDao,
    private val mapper    : UserMapper,
    private val auth      : FirebaseAuth,
    @IoDispatcher           private val io       : CoroutineDispatcher,
    @ApplicationScope       private val appScope : CoroutineScope   // scope גלובלי
) : LiveSyncRepository<UserDto, UserEntity, User>(), UserRepository {

    private var liveSyncJob: Job? = null
    /* ───────────── Live‑sync (Firestore → Room) ───────────── */
    /* Live‑sync: מאזין למסמך של המשתמש בלבד */
    /* ───────────── Live‑sync רק למסמך‑המשתמש ───────────── */

    override fun listenRemote(): Flow<Result<List<UserDto>>> =
        firestore.listenMyUserDoc(currentUid() ?: error("Not logged‑in"))
            .map { res ->
                when (res) {
                    is Result.Success -> Result.Success(listOf(res.data))
                    is Result.Failure -> Result.Failure(res.throwable)
                    Result.Loading    -> Result.Loading
                }
            }

    /** החלפה מלאה מגיעה רק מאזין‐הכל */
    override suspend fun replaceAllLocal(list: List<UserEntity>) =
        dao.replaceAll(list)

    /* ───────────── סנכרון כל המשתמשים ───────────── */

    private var allUsersSyncJob: Job? = null

    private fun startAllUsersSync() {
        if (allUsersSyncJob != null) return   // כבר רץ
        allUsersSyncJob = firestore.listenUsers()         // Flow<Result<List<UserDto>>>
            .onEach { res ->
                if (res is Result.Success) {
                    val entities = res.data.map(mapper::toEntity)
                    withContext(io) { dao.replaceAll(entities) }
                }
            }
            .launchIn(appScope)
    }

    init { startAllUsersSync() }

    /* ───── observeMe ───── */
    override fun observeMe(): Flow<Result<User>> {
        val uid = currentUid()
            ?: return flowOf(Result.Failure(IllegalStateException("Not logged‑in")))

        // מאזין למסמך האישי ומעדכן את הרשומה (לא מוחק הכול)
        liveSyncJob ?: listenRemote()
            .onEach { res ->
                if (res is Result.Success) dao.upsert(toEntity(res.data.first()))
            }
            .launchIn(appScope)

        return dao.observe(uid).map { ent ->
            ent?.let { Result.Success(toDomain(it)) } ?: Result.Loading
        }
    }

    /* ---------- observeUser (אחר) ---------- */
    override fun observeUser(uid: String): Flow<Result<User>> =
        dao.observe(uid).map { ent ->
            ent?.let { Result.Success(toDomain(ent)) }
                ?: Result.Failure(NoSuchElementException("User $uid not found"))
        }


    /* ───────────── Direct getters ───────────── */

    override suspend fun getMe(): Result<User> = withContext(io) {
        runCatching {
            val uid = currentUid() ?: throw IllegalStateException("Not logged‑in")
            dao.get(uid)?.let(mapper::toDomain) ?: run {             // ← cache miss
                val dto    = firestore.getMe()
                val entity = mapper.toEntity(dto)
                dao.upsert(entity)
                mapper.toDomain(entity)
            }
        }.fold({ Result.Success(it) }, { Result.Failure(it) })
    }

    override suspend fun getUser(uid: String): Result<User> = withContext(io) {
        runCatching {
            dao.get(uid)?.let(mapper::toDomain) ?: run {
                val dto    = firestore.getUser(uid)
                val entity = mapper.toEntity(dto)
                dao.upsert(entity)
                mapper.toDomain(entity)
            }
        }.fold({ Result.Success(it) }, { Result.Failure(it) })
    }

    /* ───────────── Auth / Profile ───────────── */

    override suspend fun signInOrUpWithEmail(
        email: String,
        password: String,
        displayName: String?,
        bio: String?
    ): Result<User> = withContext(io) {

        val isSignup = !displayName.isNullOrBlank() || !bio.isNullOrBlank()

        suspend fun authFlow(): String =
            if (isSignup)
                auth.createUserWithEmailAndPassword(email, password).await().user!!.uid
            else
                auth.signInWithEmailAndPassword(email, password).await().user!!.uid

        runCatching {
            authFlow()                                               // ← Firebase‑Auth
            val dto = functions.signInOrUp(
                email       = email,
                password    = password,
                displayName = if (isSignup) displayName else null,
                bio         = if (isSignup) bio         else null
            )
            val entity = mapper.toEntity(dto)
            dao.upsert(entity)
            mapper.toDomain(entity)
        }.fold({ Result.Success(it) }, { Result.Failure(it) })
    }

    override suspend fun updateProfile(
        displayName: String?, bio: String?, photoUrl: String?
    ): Result<Unit> = withContext(io) {
        runCatching { functions.updateProfile(displayName, bio, photoUrl) }
            .fold({ Result.Success(Unit) }, { Result.Failure(it) })
    }

    override suspend fun deleteMyAccount(): Result<Unit> = withContext(io) {
        runCatching { functions.deleteMyAccount() }
            .fold({ Result.Success(Unit) }, { Result.Failure(it) })
    }

    override suspend fun touchLogin(): Result<Unit> = withContext(io) {
        runCatching { functions.touchLogin() }
            .fold({ Result.Success(Unit) }, { Result.Failure(it) })
    }

    /* ───────────── Misc ───────────── */

    private suspend fun <T> retry(times: Int, delayMs: Long, block: suspend () -> T): T {
        repeat(times - 1) { try  { return block() } catch (_: Exception) { delay(delayMs) } }
        return block()
    }


    override fun toEntity(dto: UserDto)  = mapper.toEntity(dto)
    override fun toDomain(e: UserEntity) = mapper.toDomain(e)
    override fun currentUid(): String?   = auth.currentUser?.uid




}

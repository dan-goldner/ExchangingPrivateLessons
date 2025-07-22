package com.example.exchangingprivatelessons.data.remote.firestore

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.data.remote.dto.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await


@Singleton
class FirestoreDataSource @Inject constructor(
    private val db  : FirebaseFirestore,
    private val auth: FirebaseAuth
) {


    /** משלים את השדה uid (אם קיים) במסמך שהגיע מפיירסטור */
    @PublishedApi   // <‑‑‑‑‑‑‑‑‑‑‑‑‑‑‑‑
    internal fun <T> T.withUid(id: String): T = apply {
        when (this) {
            is UserDto -> this.id = id
        }
    }

    /* ─────────── One‑shot (suspend) ─────────── */

    suspend inline fun <reified T> getDoc(ref: DocumentReference): Result<T> = try {
        val snap = ref.get().await()
        if (snap.exists())
            Result.Success(snap.toObject(T::class.java)!!.withUid(snap.id))
        else
            Result.Failure(NoSuchElementException("Doc ${ref.path} not found"))
    } catch (t: Throwable) { Result.Failure(t) }

    suspend inline fun <reified T> getCollection(query: Query): Result<List<T>> = try {
        val list = query.get().await().documents
            .mapNotNull { it.toObject(T::class.java)?.withUid(it.id) }
        Result.Success(list)
    } catch (t: Throwable) { Result.Failure(t) }

    /* ─────────── Realtime (Flow) ─────────── */

    inline fun <reified T> listenDoc(ref: DocumentReference): Flow<Result<T>> = callbackFlow {
        val reg = ref.addSnapshotListener { s, e ->
            when {
                e != null -> trySend(Result.Failure(e))
                s != null && s.exists() ->
                    trySend(Result.Success(s.toObject(T::class.java)!!.withUid(s.id)))
            }
        }
        awaitClose { reg.remove() }
    }

    inline fun <reified T> listenCollection(query: Query): Flow<Result<List<T>>> = callbackFlow {
        val reg = query.addSnapshotListener { s, e ->
            when {
                e != null -> trySend(Result.Failure(e))
                s != null -> trySend(
                    Result.Success(
                        s.documents.mapNotNull { it.toObject(T::class.java)?.withUid(it.id) }
                    )
                )
            }
        }
        awaitClose { reg.remove() }
    }

    /* ─────────── Chats ─────────── */

    suspend fun getChats(): List<ChatDto> {
        val uid = auth.currentUser?.uid ?: error("User not logged‑in")
        return db.collection("chats")
            .whereArrayContains("participantIds", uid)
            .get().await().documents.mapNotNull { it.toObject(ChatDto::class.java) }
    }

    fun observeChatMessages(chatId: String): Flow<List<MessageDto>> = callbackFlow {
        val listener = db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("sentAt")
            .addSnapshotListener { qs, e ->
                when {
                    e != null -> close(e)
                    qs != null -> trySend(
                        qs.documents.mapNotNull { it.toObject(MessageDto::class.java) }
                    )
                }
            }
        awaitClose { listener.remove() }
    }


    /* ───────────── Lessons ───────────── */

    /* ─────────── Lessons ─────────── */

    suspend fun getLessons(): List<LessonDto> {
        val uid = auth.currentUser?.uid ?: error("User not logged‑in")
        return db.collection("lessons")
            .whereEqualTo("ownerId", uid)
            .get().await().documents.mapNotNull { it.toObject(LessonDto::class.java) }
    }

    suspend fun getLessonsOfferedByUser(userId: String): List<LessonDto> =
        db.collection("lessons")
            .whereEqualTo("ownerId", userId)
            .get().await().documents.mapNotNull { it.toObject(LessonDto::class.java) }

    suspend fun getLessonRequests(): List<LessonRequestDto> {
        val uid = auth.currentUser?.uid ?: error("User not logged‑in")
        return db.collection("lessonRequests")
            .whereEqualTo("requesterId", uid)
            .orderBy("requestedAt", Query.Direction.DESCENDING)
            .get().await().documents.mapNotNull { it.toObject(LessonRequestDto::class.java) }
    }

    suspend fun getRatings(lessonId: String): List<RatingDto> =
        db.collection("lessons")
            .document(lessonId)
            .collection("ratings")
            .get()
            .await()
            .documents
            .mapNotNull { snap ->
                snap.toObject(RatingDto::class.java)?.copy(lessonId = lessonId)
            }

    suspend fun getAllRatings(): List<RatingDto> =
        db.collectionGroup("ratings")
            .get().await().documents
            .mapNotNull { doc ->
                doc.toObject(RatingDto::class.java)
                    ?.copy(lessonId = doc.reference.parent.parent?.id ?: "")
            }


    suspend fun getTakenLessons(): List<TakenLessonDto> {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Must be logged in")
        return db.collection("takenLessons")
            .whereEqualTo("userId", uid)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(TakenLessonDto::class.java) }
    }

    /* ─────────── Users ─────────── */

    suspend fun getMe(): UserDto {
        val uid = auth.currentUser?.uid ?: error("User not logged‑in")
        return db.collection("users").document(uid).get().await()
            .toObject(UserDto::class.java)!!.apply { this.id = uid }
    }

    suspend fun getUser(uid: String): UserDto =
        db.collection("users").document(uid).get().await()
            .toObject(UserDto::class.java)!!.apply { this.id = uid }

    fun listenUsers() =
        listenCollection<UserDto>(db.collection("users"))

    fun listenMyUserDoc(uid: String) =
        listenDoc<UserDto>(db.collection("users").document(uid))
    fun listenChats() : Flow<Result<List<ChatDto>>> {
        val uid = auth.currentUser?.uid ?: error("User not logged‑in")
        return listenCollection(
            db.collection("chats").whereArrayContains("participantIds", uid)
        )
    }

    fun listenLessonRequests(): Flow<Result<List<LessonRequestDto>>> {
        val uid = auth.currentUser?.uid ?: error("User not logged‑in")
        return listenCollection(
            db.collection("lessonRequests").whereEqualTo("requesterId", uid)
        )
    }
    /* ------------------------------------------ */
}

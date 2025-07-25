package com.example.exchangingprivatelessons.data.remote.firestore

import android.util.Log
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.tasks.await


@Singleton
class FirestoreDataSource @Inject constructor(
    private val db  : FirebaseFirestore,
    private val auth: FirebaseAuth
) {


    /* ---------- helper: add id field ---------- */

    @PublishedApi
    internal fun <T> T.withUid(id: String): T = apply {
        when (this) {
            is UserDto          -> this.id = id
            is LessonDto        -> this.id = id
            is LessonRequestDto -> this.id = id
            is ChatDto          -> this.id = id
        }
    }



    /* ---------- One‑shot ---------- */
    suspend inline fun <reified T> getDoc(ref: DocumentReference): Result<T> = try {
        val s = ref.get().await()
        if (s.exists()) Result.Success(s.toObject(T::class.java)!!.withUid(s.id))
        else Result.Failure(NoSuchElementException("Doc ${ref.path} not found"))
    } catch (t: Throwable) { Result.Failure(t) }

    suspend inline fun <reified T> getCollection(q: Query): Result<List<T>> = try {
        Result.Success(
            q.get().await().documents.mapNotNull {
                it.toObject(T::class.java)?.withUid(it.id)
            })
    } catch (t: Throwable) { Result.Failure(t) }

    /* ---------- Realtime ---------- */
    inline fun <reified T> listenDoc(ref: DocumentReference): Flow<Result<T>> = callbackFlow {
        val reg = ref.addSnapshotListener { s, e ->
            when {
                e != null -> trySend(Result.Failure(e))
                s != null && s.exists() ->
                    trySend(Result.Success(s.toObject(T::class.java)!!.withUid(s.id)))
            }
        }; awaitClose { reg.remove() }
    }
    suspend fun getLessonRequest(id: String): LessonRequestDto =
        db.collection("lessonRequests").document(id).get().await()
            .toObject(LessonRequestDto::class.java)
            ?.apply { this.id = id }                          // 🔧 הוספת ה-ID הידני
            ?: error("Request $id not found")





    inline fun <reified T> listenCollection(q: Query): Flow<Result<List<T>>> = callbackFlow {
        val reg = q.addSnapshotListener { s, e ->
            when {
                e != null -> trySend(Result.Failure(e))
                s != null -> trySend(
                    Result.Success(
                        s.documents.mapNotNull { it.toObject(T::class.java)?.withUid(it.id) }
                    ))
            }
        }; awaitClose { reg.remove() }
    }

    /* ---------- users helpers ---------- */
    fun listenUsers()               = listenCollection<UserDto>(db.collection("users"))
    fun listenMyUserDoc(uid: String)= listenDoc<UserDto>(db.collection("users").document(uid))



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


    // FirestoreDataSource.kt
    suspend fun getLessons(): List<LessonDto> {
        val snaps = db.collection("lessons").get().await()
        Log.d("DBG/Remote", "Found ${snaps.size()} documents")
        return snaps.documents.mapNotNull { doc ->
            val dto = doc.toObject(LessonDto::class.java) ?: return@mapNotNull null
            dto.copy(id = doc.id).also {
                Log.d("DBG/Remote", "id=${it.id} owner=${it.ownerId} status=${it.status}")
            }
        }
    }


    suspend fun getLessonsOfferedByUser(userId: String): List<LessonDto> =
        db.collection("lessons")
            .whereEqualTo("ownerId", userId)
            .get().await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(LessonDto::class.java)?.copy(id = doc.id)
            }


    /* ---------- Lesson Requests ---------- */

    /** one‑shot –  כל הבקשות שקשורות אליי (owner *או* requester) */
    suspend fun getLessonRequests(): List<LessonRequestDto> {
        val uid = auth.currentUser?.uid ?: error("User not logged‑in")

        val sent = db.collection("lessonRequests")
            .whereEqualTo("requesterId", uid)
            .get().await()
            .documents
            .mapNotNull { it.toObject(LessonRequestDto::class.java)?.withUid(it.id) }

        val received = db.collection("lessonRequests")
            .whereEqualTo("ownerId", uid)
            .get().await()
            .documents
            .mapNotNull { it.toObject(LessonRequestDto::class.java)?.withUid(it.id) }

        return (sent + received).distinctBy { it.id }
    }



    /*
    fun listenLessonRequests(uid: String): Flow<Result<List<LessonRequestDto>>> {

        val sentFlow = listenCollection<LessonRequestDto>(
            db.collection("lessonRequests").whereEqualTo("requesterId", uid)
        )

        val receivedFlow = listenCollection<LessonRequestDto>(
            db.collection("lessonRequests").whereEqualTo("ownerId", uid)
        )

        return combine(sentFlow, receivedFlow) { sent, received ->

            // אם אחת הזרימות נכשלת – נחזיר מייד Failure
            if (sent is Result.Failure)     return@combine sent
            if (received is Result.Failure) return@combine received

            // שלב מיזוג הרשימות והסרת כפילויות
            val list = buildList {
                if (sent is Result.Success)     addAll(sent.data)
                if (received is Result.Success) addAll(received.data)
            }.distinctBy { it.id }

            Result.Success(list)
        }
    }
    */


    /* FirestoreDataSource.kt */

    fun listenSentRequests(uid: String) = listenCollection<LessonRequestDto>(
        db.collection("lessonRequests")
            .whereEqualTo("requesterId", uid)
            .orderBy("requestedAt", Query.Direction.DESCENDING)
    )

    fun listenIncomingRequests(uid: String) = listenCollection<LessonRequestDto>(
        db.collection("lessonRequests")
            .whereEqualTo("ownerId", uid)
            .orderBy("requestedAt", Query.Direction.DESCENDING)
    )




    suspend fun getLessonById(id: String): LessonDto? {
        return try {
            val snap = db.collection("lessons").document(id).get().await()
            if (snap.exists()) {
                val dto = snap.toObject(LessonDto::class.java)
                Log.d("Firestore", "Fetched lesson [$id]: $dto") // ✅ log full object
                dto?.copy(id = id)
            } else {
                Log.e("Firestore", "Lesson [$id] does not exist.")
                null
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Failed to fetch lesson [$id]: ${e.message}", e)
            null
        }
    }

    suspend fun deleteLesson(lessonId: String) {
        db.collection("lessons").document(lessonId).delete().await()
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




    /* ─────────── Users ─────────── */

    suspend fun getMe(): UserDto {
        val uid = auth.currentUser?.uid ?: error("User not logged‑in")
        return db.collection("users").document(uid).get().await()
            .toObject(UserDto::class.java)!!.apply { this.id = uid }
    }

    suspend fun getUser(uid: String): UserDto =
        db.collection("users").document(uid).get().await()
            .toObject(UserDto::class.java)!!.apply { this.id = uid }


    fun listenChats() : Flow<Result<List<ChatDto>>> {
        val uid = auth.currentUser?.uid ?: error("User not logged‑in")
        return listenCollection(
            db.collection("chats").whereArrayContains("participantIds", uid)
        )
    }




    suspend fun updateUserFields(uid: String, map: Map<String, Any?>) {
        db.collection("users").document(uid).update(map).await()
    }
}



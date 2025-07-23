package com.example.exchangingprivatelessons.data.remote.cloud

import com.example.exchangingprivatelessons.data.remote.dto.UserDto
import com.google.firebase.Timestamp
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FunctionsDataSource @Inject constructor(
    private val functions: FirebaseFunctions
) {

    /* ───────────── Low-level invoke ───────────── */
    private suspend inline fun <reified R> invoke(
        name: String,
        args: Any? = null
    ): R = functions
        .getHttpsCallable(name)
        .let { if (args == null) it.call() else it.call(args) }
        .await()
        .data as R           // לא מצליח להמיר? ייזרק Exception → נתפס בריפו


    /* ───────────── Account / User ───────────── */
    suspend fun signInOrUp(
        email: String,
        password: String,
        displayName: String?,
        bio: String?
    ): UserDto {
        val res: Map<*, *> = invoke(
            "signInOrUp",
            mapOf(
                "email"       to email,
                "password"    to password,
                "displayName" to displayName,
                "bio"         to bio
            )
        )
        return UserDto(
            id          = res["uid"]         as String,
            displayName = res["displayName"] as? String ?: "",
            email       = res["email"]       as? String ?: "",
            photoUrl    = res["photoUrl"]    as? String ?: "",
            bio         = res["bio"]         as? String ?: "",
            score       = (res["score"]      as? Number)?.toInt() ?: 0,
            createdAt   = res["createdAt"]   as? Timestamp,
            lastLoginAt = res["lastLoginAt"] as? Timestamp
        )

    }


    suspend fun deleteMyAccount() {
        // הפונקציה מחזירה Map; אנחנו פשוט מתעלמים מה‑payload
        invoke<Map<*, *>>("deleteMyAccount")
    }

    suspend fun touchLogin()      = invoke<Unit>("touchLogin")

    suspend fun updateProfile(
        displayName: String?,
        bio: String?,
        photoUrl: String?
    ) {
        invoke<Any?>(
            "updateProfile",
            mapOf("displayName" to displayName, "bio" to bio, "photoUrl" to photoUrl)
        )
    }

    /* ───────────── Lessons ───────────── */

    suspend fun createLesson(
        title: String,
        description: String,
        imageUrl: String?
    ): String {
        val res: Map<*, *> = invoke(
            "createLesson",
            mapOf("title" to title, "description" to description, "imageUrl" to imageUrl)
        )
        return res["lessonId"] as String
    }

    suspend fun updateLesson(
        lessonId: String,
        title: String?,
        description: String?,
        imageUrl: String?
    ) = invoke<Unit>(
        "updateLesson",
        mapOf(
            "lessonId"    to lessonId,
            "title"       to title,
            "description" to description,
            "imageUrl"    to imageUrl
        )
    )

    suspend fun archiveLesson(lessonId: String, archived: Boolean) =
        invoke<Unit>("archiveLesson", mapOf("lessonId" to lessonId, "archived" to archived))

    /* ───────────── Lesson Requests ───────────── */

    suspend fun createLessonRequest(lessonId: String, ownerId: String): String {
        val res: Map<*, *> = invoke(
            "createLessonRequest",
            mapOf("lessonId" to lessonId, "ownerId" to ownerId)
        )
        return res["id"] as String
    }

    suspend fun approveLessonRequest(requestId: String) =
        invoke<Unit>("approveLessonRequest", mapOf("requestId" to requestId))

    suspend fun declineLessonRequest(requestId: String) =
        invoke<Unit>("declineLessonRequest", mapOf("requestId" to requestId))

    suspend fun cancelLessonRequest(requestId: String) =
        invoke<Unit>("cancelLessonRequest", mapOf("requestId" to requestId))

    /* ───────────── Ratings ───────────── */

    suspend fun rateLesson(lessonId: String, value: Int, comment: String?) =
        invoke<Unit>(
            "rateLesson",
            mapOf("lessonId" to lessonId, "numericValue" to value, "comment" to comment)
        )

    /* ───────────── Chat ───────────── */

    suspend fun createChat(peerUid: String): String {
        val res: Map<*, *> = invoke("createChat", mapOf("peerUid" to peerUid))
        return res["chatId"] as String
    }

    suspend fun sendMessage(chatId: String, text: String) =
        invoke<Unit>("sendMessage", mapOf("chatId" to chatId, "text" to text))
}

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
    ): R {
        val result = if (args == null)
            functions.getHttpsCallable(name).call()
        else
            functions.getHttpsCallable(name).call(args)

        val data = result.await().data

        return when (R::class) {
            Unit::class -> Unit as R  // Return Unit if requested
            else        -> data as R  // Otherwise cast to R
        }
    }

    /* ───────────── Account / User ───────────── */
    suspend fun signInOrUp(
        email: String,
        password: String,
        displayName: String?,
        bio: String?
    ): UserDto {

        /* 1. בניית ה‑payload */
        val payload = mutableMapOf(
            "email"    to  email,
            "password" to  password
        ).apply {
            displayName?.let { put("displayName", it) }
            bio        ?.let { put("bio", it)         }
        }

        /* 2. קריאה ל‑Cloud Function */
        val res: Map<*, *> = invoke("signInOrUp", payload)

        /* 3. מיפוי לתוך UserDto */
        return UserDto(
            id          = res["uid"]          as String,
            displayName = res["displayName"]  as? String ?: "",
            email       = res["email"]        as? String ?: "",
            photoUrl    = res["photoUrl"]     as? String ?: "",
            bio         = res["bio"]          as? String ?: "",
            score       = (res["score"]       as? Number)?.toInt() ?: 0,
            createdAt   = res["createdAt"]    as? Timestamp,
            lastLoginAt = res["lastLoginAt"]  as? Timestamp,
            lastUpdatedAt = res["lastUpdatedAt"]  as? Timestamp      // ⬅︎ חדש
        )
    }



    suspend fun deleteMyAccount() {
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
            mapOf("displayName" to displayName, "bio" to bio)
        )
    }

    /* ───────────── Lessons ───────────── */

    suspend fun createLesson(
        title: String,
        description: String,
    ): String {
        val res: Map<*, *> = invoke(
            "createLesson",
            mapOf("title" to title, "description" to description)
        )
        return res["lessonId"] as String
    }

    suspend fun updateLesson(
        lessonId: String,
        title: String?,
        description: String?,
    ) = invoke<Unit>(
        "updateLesson",
        mapOf(
            "lessonId"    to lessonId,
            "title"       to title,
            "description" to description,
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

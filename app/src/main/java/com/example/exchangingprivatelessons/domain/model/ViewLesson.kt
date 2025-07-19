package com.example.exchangingprivatelessons.domain.model

/**
 * Convenience projection returned by [ObserveLessons] use-case.
 * Combines base lesson, average rating & requester’s state
 * (e.g. whether the current user already requested / took / owns it).
 *
 * Keeps UI logic out of XML / Compose.
 */
data class ViewLesson(
    val lesson: Lesson,
    val average: Float,
    val myRequestStatus: RequestStatus?,   // null = no request
    val isTaken: Boolean,                  // I’m already a student
    val isMine: Boolean,
    val id:           String,
    val title:        String,
    val description:  String,
    val ownerId: String,
    val ownerName:    String,
    val imageUrl:     String?,          // תמונת המורה
    val createdAt:    Long,
    val ratingAvg:    Double,
    val ratingCount:  Int,
    val canEdit:      Boolean,
    val canRequest:   Boolean,
    val canRate:      Boolean,

    val archived   : Boolean,   // ← מצב נוכחי
    val canArchive : Boolean    // ← האם הכפתור יוצג
)

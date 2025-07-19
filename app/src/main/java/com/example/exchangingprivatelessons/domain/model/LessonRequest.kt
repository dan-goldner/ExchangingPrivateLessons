package com.example.exchangingprivatelessons.domain.model

data class LessonRequest(
    val id: String = "",
    val lessonId: String,
    val ownerId: String,        // lesson owner
    val requesterId: String,    // student asking for the lesson

    val status: RequestStatus,
    val requestedAt: Long,
    val respondedAt: Long? = null,
)

enum class RequestStatus { Pending, Approved, Declined }

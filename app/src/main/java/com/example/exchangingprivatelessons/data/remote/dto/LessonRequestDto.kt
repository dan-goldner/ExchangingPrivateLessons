// data/remote/dto/LessonRequestDto.kt
package com.example.exchangingprivatelessons.data.remote.dto

import com.example.exchangingprivatelessons.domain.model.RequestStatus
import com.google.firebase.Timestamp   // ⬅️ חדש

data class LessonRequestDto(
    val id          : String          = "",
    val lessonId    : String          = "",
    val ownerId     : String          = "",
    val requesterId : String          = "",
    val status      : RequestStatus   = RequestStatus.Pending,
    val requestedAt : Timestamp?      = null,   // ⬅️ היה Long?
    val respondedAt : Timestamp?      = null    // ⬅️ היה Long?
)

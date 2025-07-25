// data/remote/dto/LessonDto.kt
package com.example.exchangingprivatelessons.data.remote.dto

import com.example.exchangingprivatelessons.domain.model.LessonStatus
import com.google.firebase.Timestamp

data class LessonDto(
    var id          : String          = "",
    val ownerId     : String          = "",
    val title       : String          = "",
    val description : String          = "",
    val status      : LessonStatus    = LessonStatus.Active,
    val ratingSum   : Int             = 0,
    val ratingCount : Int             = 0,
    val createdAt   : Timestamp?      = null,   // ⬅️ היה Long?
    val lastUpdated : Timestamp?      = null    // ⬅️ היה Long?
)

// data/remote/dto/TakenLessonDto.kt
package com.example.exchangingprivatelessons.data.remote.dto

import com.google.firebase.Timestamp   // ⬅️ חדש

data class TakenLessonDto(
    val lesson        : LessonDto,
    val ownerName     : String   = "",
    val ownerPhotoUrl : String?  = null,
    val takenAt       : Timestamp? = null,  // ⬅️ היה Long
    val canRate       : Boolean  = false
)

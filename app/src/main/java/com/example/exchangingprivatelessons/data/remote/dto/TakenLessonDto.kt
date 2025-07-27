// data/remote/dto/TakenLessonDto.kt
package com.example.exchangingprivatelessons.data.remote.dto

import com.google.firebase.Timestamp

// data/remote/dto/TakenLessonDto.kt
data class TakenLessonDto(
    var lessonId      : String = "",
    var takenAt       : Timestamp? = null,
    var lesson        : LessonDto? = null,
    var canRate       : Boolean    = false,
    var ownerName     : String?    = null,
    var ownerPhotoUrl : String?    = null
)

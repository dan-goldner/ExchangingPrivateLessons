// data/remote/dto/RatingDto.kt
package com.example.exchangingprivatelessons.data.remote.dto

import com.google.firebase.Timestamp   // ⬅️ חדש

data class RatingDto(
    val lessonId    : String  = "",
    val uid         : String  = "",     // rater UID
    val numericValue: Int     = 0,      // 1‑5
    val comment     : String? = null,
    val ratedAt     : Timestamp? = null // ⬅️ היה Long?
)

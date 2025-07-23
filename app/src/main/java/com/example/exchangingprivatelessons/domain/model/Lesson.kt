package com.example.exchangingprivatelessons.domain.model

import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Serializable
import java.security.Timestamp

@Serializable
data class Lesson(
    val id: String = "",                // convenience for UI diffing
    val ownerId: String,

    val title: String,
    val description: String,
    val imageUrl: String?,

    val status: LessonStatus,
    val ratingSum: Int,
    val ratingCount: Int,

    @get:ServerTimestamp
    val createdAt: Long,
    val lastUpdated: Long,
) {
    val avgRating: Float
        get() = if (ratingCount == 0) 0f else ratingSum.toFloat() / ratingCount
}

enum class LessonStatus { Active, Archived }

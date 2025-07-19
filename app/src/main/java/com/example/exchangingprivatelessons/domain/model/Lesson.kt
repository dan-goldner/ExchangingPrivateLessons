package com.example.exchangingprivatelessons.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Lesson(
    val id: String = "",                // convenience for UI diffing
    val ownerId: String,

    val title: String,
    val description: String,
    val imageUrl: String,

    val status: LessonStatus,
    val ratingSum: Int,
    val ratingCount: Int,

    val createdAt: Long,
    val lastUpdated: Long,
) {
    val avgRating: Float
        get() = if (ratingCount == 0) 0f else ratingSum.toFloat() / ratingCount
}

enum class LessonStatus { Active, Archived }

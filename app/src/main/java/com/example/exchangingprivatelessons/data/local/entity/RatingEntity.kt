package com.example.exchangingprivatelessons.data.local.entity

import androidx.room.Entity
import java.util.Date

@Entity(
    tableName = "ratings",
    primaryKeys = ["lessonId", "userId"]
)
data class RatingEntity(
    val lessonId: String,
    val userId: String,
    val numericValue: Int,      // 1–5
    val comment: String?,
    val ratedAt: Date?
)

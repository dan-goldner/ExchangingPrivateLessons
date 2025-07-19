package com.example.exchangingprivatelessons.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "lesson_requests")
data class LessonRequestEntity(
    @PrimaryKey val id: String,
    val lessonId: String,
    val ownerId: String,
    val requesterId: String,
    val status: String,          // "Pending" | "Approved" | "Declined"
    val requestedAt: Date?,
    val respondedAt: Date?
)

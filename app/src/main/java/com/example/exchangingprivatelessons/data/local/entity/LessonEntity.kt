package com.example.exchangingprivatelessons.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val status: String,               // "Active" | "Archived"
    val ratingSum: Int,
    val ratingCount: Int,
    val createdAt: Date?,
    val lastUpdated: Date?
)

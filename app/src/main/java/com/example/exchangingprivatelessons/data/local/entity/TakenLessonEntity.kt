package com.example.exchangingprivatelessons.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "taken_lessons")
data class TakenLessonEntity(
    @PrimaryKey val lessonJson: String,
    val ownerName:     String,
    val ownerPhotoUrl: String?,
    val takenAt:       Date?,
    val canRate:       Boolean,
    val status:        String
)

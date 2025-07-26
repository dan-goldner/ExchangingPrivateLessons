package com.example.exchangingprivatelessons.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date


@Entity(tableName = "taken_lessons")
data class TakenLessonEntity(
    @PrimaryKey val lessonId      : String,   // ← doc.id
    val lessonJson                : String,   // LessonDto as JSON
    val ownerName                 : String,
    val ownerPhotoUrl             : String?,
    val canRate                   : Boolean,
    val takenAt                   : Long?     // epoch ms
)

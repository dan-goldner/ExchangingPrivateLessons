package com.example.exchangingprivatelessons.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.exchangingprivatelessons.model.Lesson   // ← מודל הדומיין

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String
)

/* ---- פונקציות ההמרה (Extension) ---- */
fun LessonEntity.toModel() = Lesson(id, title, description)

fun Lesson.toEntity()       = LessonEntity(id, title, description)

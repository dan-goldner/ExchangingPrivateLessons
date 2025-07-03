package com.example.exchangingprivatelessons.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.exchangingprivatelessons.model.Lesson

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val title      : String,
    val description: String,
    val ownerId    : String,
    val ownerName  : String      // נשמר כדי להציג גם בלי אינטרנט
)

/* המרות בין Room ↔︎ Model ----------------------------------------------- */
fun LessonEntity.toModel() = Lesson(
    id          = id,
    title       = title,
    description = description,
    ownerId     = ownerId,
    ownerName   = ownerName
)

/* ownerImageUrl לא נשמר – לכן מעבירים מחרוזת ריקה */
fun Lesson.toEntity() = LessonEntity(id, title, description, ownerId, ownerName)

// LessonItem.kt
package com.example.exchangingprivatelessons.ui.lesson

import android.content.Context
import com.example.exchangingprivatelessons.domain.model.ViewLesson

data class LessonItem(
    val id: String,
    val title: String,
    val date: String,
    val rating: String,
    val imageUrl: String?,
    val archived: Boolean,
    val canArchive: Boolean
)

fun ViewLesson.toItem(ctx: Context): LessonItem = LessonItem(
    id          = id,
    title       = title,
    date        = /* formatDate(createdAt) */ createdAt.toString(),
    rating      = "%.1f".format(ratingAvg),
    imageUrl    = imageUrl,
    archived    = archived,
    canArchive  = canArchive
)

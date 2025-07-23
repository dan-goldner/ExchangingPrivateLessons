// LessonItem.kt
package com.example.exchangingprivatelessons.ui.lesson

import android.content.Context
import com.example.exchangingprivatelessons.domain.model.ViewLesson
import java.text.SimpleDateFormat
import java.util.*

private fun formatDate(date: Long?): String {
    if (date == null) return ""
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(date)
}

data class LessonItem(
    val id: String,
    val title: String,
    val date: String,
    val rating: String,
    val imageUrl: String?,
    val archived: Boolean,
    val canArchive: Boolean,
    val description: String,
)

fun ViewLesson.toItem(ctx: Context): LessonItem = LessonItem(
    id          = id,
    title       = title,
    date        = formatDate(createdAt),  /*createdAt.toString()*/
    rating      = "%.1f".format(ratingAvg),
    imageUrl    = imageUrl,
    archived    = archived,
    canArchive  = canArchive,
    description = description
)

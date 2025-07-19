// data/remote/dto/LessonDto.kt
package com.example.exchangingprivatelessons.data.remote.dto

import kotlinx.serialization.Serializable
import com.example.exchangingprivatelessons.domain.model.LessonStatus

@Serializable
data class LessonDto(
    val id: String,
    val ownerId: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val status: LessonStatus,
    val ratingSum: Int,
    val ratingCount: Int,
    val createdAt: Long?,
    val lastUpdated: Long?
)

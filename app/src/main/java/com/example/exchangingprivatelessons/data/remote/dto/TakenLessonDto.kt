/* ───────────────────────────  TakenLesson  ────────────────────────── */
package com.example.exchangingprivatelessons.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TakenLessonDto(
    val lesson:        LessonDto,
    val ownerName:     String = "",
    val ownerPhotoUrl: String? = null,
    val takenAt:       Long   = 0L,
    val canRate:       Boolean = false
)

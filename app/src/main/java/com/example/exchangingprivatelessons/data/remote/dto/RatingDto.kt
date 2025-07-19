/* ──────────────────────────────  Rating  ──────────────────────────── */
package com.example.exchangingprivatelessons.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RatingDto(
    val lessonId: String = "",      // ➊  ← מזהים איזה שיעור דורג
    val uid: String = "",           // rater’s uid
    val numericValue: Int = 0,      // 1‑5
    val comment: String? = null,
    val ratedAt: Long? = null
)


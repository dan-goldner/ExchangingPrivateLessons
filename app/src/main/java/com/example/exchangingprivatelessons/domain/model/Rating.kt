package com.example.exchangingprivatelessons.domain.model

data class Rating(
    val lessonId: String,
    val userId: String,     // who rated
    val numericValue: Int,  // 1-5
    val comment: String?,
    val ratedAt: Long,
)

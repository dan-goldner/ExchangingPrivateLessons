package com.example.exchangingprivatelessons.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String,
    val bio: String,
    val score: Int,
    val createdAt: Long,
    val lastLoginAt: Long,
    val lastUpdatedAt: Long? = null,
)

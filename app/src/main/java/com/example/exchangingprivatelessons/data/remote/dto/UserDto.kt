/* ───────────────────────────────  User  ───────────────────────────── */
package com.example.exchangingprivatelessons.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val bio: String = "",
    val score: Int = 0,
    val createdAt: Long? = null,
    val lastLoginAt: Long? = null
)

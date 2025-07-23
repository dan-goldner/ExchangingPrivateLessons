/* ───────────────────────────────  User  ───────────────────────────── */
package com.example.exchangingprivatelessons.data.remote.dto

import kotlinx.serialization.Serializable
import com.google.firebase.Timestamp


data class UserDto(
    var id          : String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val bio: String = "",
    val score: Int = 0,
    val createdAt: Timestamp? = null,
    val lastLoginAt: Timestamp? = null,
    val lastUpdatedAt: Timestamp? = null
)

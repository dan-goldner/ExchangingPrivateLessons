package com.example.exchangingprivatelessons.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val email: String,
    val photoUrl: String,
    val bio: String,
    val score: Int,
    val createdAt: Date?,
    val lastLoginAt: Date?,
    val lastUpdated: Date? = null
)

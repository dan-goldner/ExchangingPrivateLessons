package com.example.exchangingprivatelessons.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey            val id: String,
    /** המשתתף הראשון (Index 0 ברשימה) */
    val participantIdA: String,
    /** המשתתף השני (Index 1 ברשימה) */
    val participantIdB: String,

    val createdAt:  Date?,
    val lastMessage: String,
    val lastMessageAt: Date?
)

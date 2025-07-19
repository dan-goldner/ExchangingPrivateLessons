package com.example.exchangingprivatelessons.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val participantIdA: String,
    val participantIdB: String,
    val createdAt: Date,
    val lastMessage: String,
    val lastMessageAt: Date?
)

/* ChatDto.kt */
package com.example.exchangingprivatelessons.data.remote.dto

import com.google.firebase.Timestamp

data class ChatDto(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val lastMessage: String = "",
    val createdAt: Timestamp? = null,
    val lastMessageAt: Timestamp? = null
)

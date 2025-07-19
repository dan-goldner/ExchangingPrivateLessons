/* ─────────────────────────────  Chat  ────────────────────────────── */
package com.example.exchangingprivatelessons.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatDto(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val lastMessage: String = "",
    val createdAt: Long = 0L,
    val lastMessageAt: Long? = null
)

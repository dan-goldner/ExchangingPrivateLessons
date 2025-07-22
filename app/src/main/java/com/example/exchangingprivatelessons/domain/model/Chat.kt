package com.example.exchangingprivatelessons.domain.model

data class Chat(
    val id: String,
    val participantIds: List<String>,
    val lastMessage: String?,
    /** Unix epoch millis */
    val createdAt: Long,
    /** Unix epoch millis, nullable */
    val lastMessageAt: Long?,
    /** שם‑הצד‑השני ל‑UI (נטען בריפו) */
    val peerName: String = ""
)

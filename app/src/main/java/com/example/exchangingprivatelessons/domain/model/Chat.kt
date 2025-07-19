package com.example.exchangingprivatelessons.domain.model


data class Chat(
    val id: String,
    /** שני ה‑UID‑ים של המשתתפים (ממוינים באותו סדר כמו ב‑Firestore) */
    val participantIds: List<String>,
    /** שם‑התצוגה של הצד השני – נטען בריפו לצורכי UI */
    val peerName: String? = null,

    val lastMessage: String? = null,
    val lastMessageAt: Long? = null
)

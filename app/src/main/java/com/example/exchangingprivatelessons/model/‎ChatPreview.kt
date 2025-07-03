package com.example.exchangingprivatelessons.model

data class ChatPreview(
    val id: String,
    val users: List<String>,
    val lastMsg: String,
    val lastMsgAt: Long
)

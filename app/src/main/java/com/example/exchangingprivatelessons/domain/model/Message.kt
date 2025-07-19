package com.example.exchangingprivatelessons.domain.model

data class Message(
    val id: String = "",
    val chatId: String,
    val senderId: String,
    val text: String,
    val sentAt: Long,
)

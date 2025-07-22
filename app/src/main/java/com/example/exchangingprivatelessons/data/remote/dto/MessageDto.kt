/* ──────────────────── MessageDto ─────────────────── */
package com.example.exchangingprivatelessons.data.remote.dto

import com.google.firebase.Timestamp



data class MessageDto(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val text: String = "",
    val sentAt: Timestamp? = null
)

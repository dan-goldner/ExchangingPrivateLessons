/* ─────────────────────────────  Message  ──────────────────────────── */
package com.example.exchangingprivatelessons.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val text: String = "",
    val sentAt: Long? = null
)

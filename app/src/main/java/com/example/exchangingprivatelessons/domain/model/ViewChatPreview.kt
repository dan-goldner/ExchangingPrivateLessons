package com.example.exchangingprivatelessons.domain.model

import com.example.exchangingprivatelessons.common.util.pretty
import java.util.*

/** “Projection” מוכן לתצוגת רשימת הצ’אטים. */
data class ViewChatPreview(
    val chatId: String,
    val peerName: String,
    val lastMessage: String,
    val lastMessageAt: Date?
) {
    val formattedTime: String
        get() = lastMessageAt?.time?.pretty() ?: ""
}

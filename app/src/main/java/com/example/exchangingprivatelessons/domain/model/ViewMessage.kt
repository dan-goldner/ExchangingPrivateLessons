package com.example.exchangingprivatelessons.domain.model

import com.example.exchangingprivatelessons.data.remote.dto.MessageDto
import com.example.exchangingprivatelessons.common.util.asClockOrDate

data class ViewMessage(
    val id: String,
    val senderId: String,
    val text: String,
    val time: String          // למשל "21:37" או "Yesterday"
) {
    companion object {
        fun from(message: Message, myUid: String): ViewMessage =
            ViewMessage(
                id        = message.id,
                senderId  = message.senderId,
                text      = message.text,
                time      = message.sentAt.asClockOrDate()
            )
    }

}
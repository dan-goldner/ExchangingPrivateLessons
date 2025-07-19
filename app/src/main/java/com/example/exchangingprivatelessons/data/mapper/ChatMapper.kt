/* ───────────  ChatMapper  ─────────── */
package com.example.exchangingprivatelessons.data.mapper

import com.example.exchangingprivatelessons.common.util.TimestampConverter
import com.example.exchangingprivatelessons.data.local.entity.ChatEntity
import com.example.exchangingprivatelessons.data.remote.dto.ChatDto
import com.example.exchangingprivatelessons.domain.model.Chat
import org.mapstruct.*
import java.util.Date

@Mapper(
    componentModel = "kotlin",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = [TimestampConverter::class],          // המרות Long ↔ Date
    builder = Builder(disableBuilder = true)
)
abstract class ChatMapper {

    /* ---------- Entity ⇢ Domain ---------- */
    fun toDomain(entity: ChatEntity): Chat = Chat(
        id             = entity.id,
        participantIds = listOfNotNull(entity.participantIdA, entity.participantIdB),
        lastMessage    = entity.lastMessage,
        lastMessageAt  = TimestampConverter.toEpoch(entity.lastMessageAt),
        peerName       = ""                              // מתמלא בריפו
    )

    /* ---------- Domain ⇢ Entity ---------- */
    fun toEntity(domain: Chat): ChatEntity = ChatEntity(
        id             = domain.id,
        participantIdA = domain.participantIds.getOrNull(0) ?: "",
        participantIdB = domain.participantIds.getOrNull(1) ?: "",
        createdAt      = Date(),                         // השרת יקבע בפועל
        lastMessage    = domain.lastMessage ?: "",
        lastMessageAt  = domain.lastMessageAt?.let(TimestampConverter::toDate)
    )

    /* ---------- DTO ⇄ Domain (MapStruct מייצר) ---------- */
    abstract fun toDomain(dto: ChatDto): Chat
    abstract fun toDto(domain: Chat): ChatDto

    /* ---------- DTO ⇢ Entity  – מימוש ידני ---------- */
    fun toEntity(dto: ChatDto): ChatEntity = ChatEntity(
        id             = dto.id,
        participantIdA = dto.participantIds.getOrNull(0) ?: "",
        participantIdB = dto.participantIds.getOrNull(1) ?: "",
        createdAt      = TimestampConverter.toDateNonNull(dto.createdAt),
        lastMessage    = dto.lastMessage,
        lastMessageAt  = dto.lastMessageAt?.let(TimestampConverter::toDate)
    )
}

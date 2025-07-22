/* ─────────── ChatMapper (Fixed) ─────────── */
package com.example.exchangingprivatelessons.data.mapper

import com.example.exchangingprivatelessons.common.util.TimestampConverter
import com.example.exchangingprivatelessons.data.local.entity.ChatEntity
import com.example.exchangingprivatelessons.data.remote.dto.ChatDto
import com.example.exchangingprivatelessons.domain.model.Chat
import org.mapstruct.*

@Mapper(
    componentModel = "kotlin",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = [TimestampConverter::class],
    builder = Builder(disableBuilder = true)
)
abstract class ChatMapper {

    /* ---------- DTO ➜ Domain (MapStruct) ---------- */
    @Mappings(
        Mapping(source = "createdAt",     target = "createdAt",
            qualifiedByName = ["tsToEpochNullable"]),
        Mapping(source = "lastMessageAt", target = "lastMessageAt",
            qualifiedByName = ["tsToEpochNullable"]),
        Mapping(target = "peerName", ignore = true)
    )
    abstract fun dtoToDomain(dto: ChatDto): Chat

    // Alias for repository compatibility
    fun toDomain(dto: ChatDto): Chat = dtoToDomain(dto)

    /* ---------- Domain ➜ DTO  (ידני) ---------- */
    fun domainToDto(domain: Chat): ChatDto = ChatDto(
        id             = domain.id,
        participantIds = domain.participantIds,
        lastMessage    = domain.lastMessage ?: "",
        createdAt      = TimestampConverter.epochToTsNullable(domain.createdAt),
        lastMessageAt  = TimestampConverter.epochToTsNullable(domain.lastMessageAt)
    )

    /* ---------- Entity ↔ Domain (ידני – בגלל פיצול IDs) ---------- */

    fun entityToDomain(entity: ChatEntity): Chat = Chat(
        id             = entity.id,
        participantIds = listOfNotNull(entity.participantIdA, entity.participantIdB),
        lastMessage    = entity.lastMessage,
        // ⬇︎  השורה הבעייתית
        createdAt = TimestampConverter.toEpochNullable(entity.createdAt) ?: 0L,
        lastMessageAt  = TimestampConverter.toEpochNullable(entity.lastMessageAt),
        peerName       = ""
    )


    // Alias for repository compatibility
    fun toDomain(entity: ChatEntity): Chat = entityToDomain(entity)

    fun domainToEntity(domain: Chat): ChatEntity = ChatEntity(
        id             = domain.id,
        participantIdA = domain.participantIds.getOrNull(0) ?: "",
        participantIdB = domain.participantIds.getOrNull(1) ?: "",
        createdAt      = TimestampConverter.toDateNonNull(domain.createdAt),
        lastMessage    = domain.lastMessage ?: "",
        lastMessageAt  = TimestampConverter.toDateNullable(domain.lastMessageAt)
    )

    /* ---------- DTO → Entity (ידני) ---------- */

    fun dtoToEntity(dto: ChatDto): ChatEntity = ChatEntity(
        id             = dto.id,
        participantIdA = dto.participantIds.getOrNull(0) ?: "",
        participantIdB = dto.participantIds.getOrNull(1) ?: "",
        createdAt      = TimestampConverter.tsToDateNullable(dto.createdAt) ?: error("createdAt missing"),
        lastMessage    = dto.lastMessage,
        lastMessageAt  = TimestampConverter.tsToDateNullable(dto.lastMessageAt)
    )

    // Alias for repository compatibility
    fun toEntity(dto: ChatDto): ChatEntity = dtoToEntity(dto)
}
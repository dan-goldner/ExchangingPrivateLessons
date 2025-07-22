package com.example.exchangingprivatelessons.data.mapper

import com.example.exchangingprivatelessons.common.util.TimestampConverter
import com.example.exchangingprivatelessons.data.local.entity.MessageEntity
import com.example.exchangingprivatelessons.data.remote.dto.MessageDto
import com.example.exchangingprivatelessons.domain.model.Message
import org.mapstruct.*

@Mapper(
    componentModel = "kotlin",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = [TimestampConverter::class],
    builder = Builder(disableBuilder = true)
)
interface MessageMapper {

    /* ---------- DTO ➜ Domain ---------- */
    @Mapping(source = "sentAt", target = "sentAt",
        qualifiedByName = ["tsToEpochNonNull"])
    fun toDomain(dto: MessageDto): Message


    /* ---------- Domain ➜ DTO  (ידני) ---------- */
    fun toDto(domain: Message): MessageDto = MessageDto(
        id       = domain.id,
        chatId   = domain.chatId,
        senderId = domain.senderId,
        text     = domain.text,
        sentAt   = TimestampConverter.epochToTsNullable(domain.sentAt)
    )


    /* ---------- Entity ↔ Domain ---------- */
    @Mapping(source = "sentAt", target = "sentAt",
        qualifiedByName = ["toEpochNullable"])
    fun toDomain(entity: MessageEntity): Message

    @Mapping(source = "sentAt", target = "sentAt",
        qualifiedByName = ["toDateNullable"])
    fun toEntity(domain: Message): MessageEntity


    /* ---------- DTO ↔ Entity ---------- */
    @Mapping(source = "sentAt", target = "sentAt",
        qualifiedByName = ["tsToDateNullable"])
    fun toEntity(dto: MessageDto): MessageEntity

    /* Entity ➜ DTO – ידני */
    fun toDto(entity: MessageEntity): MessageDto = MessageDto(
        id       = entity.id,
        chatId   = entity.chatId,
        senderId = entity.senderId,
        text     = entity.text,
        sentAt   = TimestampConverter.dateToTsNullable(entity.sentAt)
    )
}

package com.example.exchangingprivatelessons.data.mapper

import com.example.exchangingprivatelessons.common.util.TimestampConverter
import com.example.exchangingprivatelessons.data.local.entity.MessageEntity
import com.example.exchangingprivatelessons.data.remote.dto.MessageDto
import com.example.exchangingprivatelessons.domain.model.Message
import org.mapstruct.*

@Mapper(
    componentModel = "kotlin",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = [TimestampConverter::class]
)
interface MessageMapper {

    /* Entity ➜ Domain */
    @Mapping(source = "sentAt", target = "sentAt", qualifiedByName = ["toEpochNonNull"])
    fun toDomain(entity: MessageEntity): Message

    /* Domain ➜ Entity */
    @Mapping(source = "sentAt", target = "sentAt", qualifiedByName = ["toDateNonNull"])
    fun toEntity(domain: Message): MessageEntity


    /* DTO ↔ Domain (Long ↔ Long) */
    fun toDomain(dto: MessageDto): Message
    fun toDto(domain: Message): MessageDto


    /* DTO ➜ Entity */
    @Mapping(source = "sentAt", target = "sentAt", qualifiedByName = ["toDateNonNull"])
    fun toEntity(dto: MessageDto): MessageEntity

    /* Entity ➜ DTO – בלי אנוטציה */
    fun toDto(entity: MessageEntity): MessageDto
}

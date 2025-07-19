package com.example.exchangingprivatelessons.data.mapper

import com.example.exchangingprivatelessons.common.util.TimestampConverter
import com.example.exchangingprivatelessons.data.local.entity.LessonRequestEntity
import com.example.exchangingprivatelessons.data.remote.dto.LessonRequestDto
import com.example.exchangingprivatelessons.domain.model.LessonRequest
import org.mapstruct.*

@Mapper(
    componentModel = "kotlin",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = [TimestampConverter::class],
    builder = Builder(disableBuilder = true)
)
abstract class LessonRequestMapper {

    /* ---------- Entity ⇢ Domain ---------- */
    @Mappings(
        Mapping(source = "requestedAt", target = "requestedAt", qualifiedByName = ["toEpochNullable"]),
        Mapping(source = "respondedAt", target = "respondedAt", qualifiedByName = ["toEpochNullable"])
    )
    abstract fun toDomain(entity: LessonRequestEntity): LessonRequest

    /* ---------- Domain ⇢ Entity ---------- */
    @Mappings(
        Mapping(source = "requestedAt", target = "requestedAt", qualifiedByName = ["toDateNullable"]),
        Mapping(source = "respondedAt", target = "respondedAt", qualifiedByName = ["toDateNullable"])
    )
    abstract fun toEntity(domain: LessonRequest): LessonRequestEntity


    /* ===== DTO המרות ידניות ===== */
    fun toDomain(dto: LessonRequestDto): LessonRequest = LessonRequest(
        id           = dto.id,
        lessonId     = dto.lessonId,
        ownerId      = dto.ownerId,
        requesterId  = dto.requesterId,
        status       = dto.status,
        requestedAt  = dto.requestedAt ?: 0L,
        respondedAt  = dto.respondedAt
    )

    fun toEntity(dto: LessonRequestDto): LessonRequestEntity = LessonRequestEntity(
        id           = dto.id,
        lessonId     = dto.lessonId,
        ownerId      = dto.ownerId,
        requesterId  = dto.requesterId,
        status       = dto.status.name,
        requestedAt  = TimestampConverter.toDateNullable(dto.requestedAt),
        respondedAt  = TimestampConverter.toDateNullable(dto.respondedAt)
    )
}

/* ─────────── LessonRequestMapper ─────────── */
package com.example.exchangingprivatelessons.data.mapper

import com.example.exchangingprivatelessons.common.util.TimestampConverter
import com.example.exchangingprivatelessons.data.local.entity.LessonRequestEntity
import com.example.exchangingprivatelessons.data.remote.dto.LessonRequestDto
import com.example.exchangingprivatelessons.domain.model.LessonRequest
import com.example.exchangingprivatelessons.domain.model.RequestStatus
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
        Mapping(source = "requestedAt", target = "requestedAt",
            qualifiedByName = ["toEpochNullable"]),
        Mapping(source = "respondedAt", target = "respondedAt",
            qualifiedByName = ["toEpochNullable"]),
        Mapping(source = "status",      target = "status",
            qualifiedByName = ["stringToStatus"])
    )
    abstract fun toDomain(entity: LessonRequestEntity): LessonRequest

    /* ---------- Domain ⇢ Entity ---------- */
    @Mappings(
        Mapping(source = "requestedAt", target = "requestedAt",
            qualifiedByName = ["toDateNullable"]),
        Mapping(source = "respondedAt", target = "respondedAt",
            qualifiedByName = ["toDateNullable"]),
        Mapping(source = "status",      target = "status",
            qualifiedByName = ["statusToString"])
    )
    abstract fun toEntity(domain: LessonRequest): LessonRequestEntity


    /* ---------- DTO ⇢ Domain (Timestamp → Long) ---------- */
    fun toDomain(dto: LessonRequestDto): LessonRequest = LessonRequest(
        id          = dto.id,
        lessonId    = dto.lessonId,
        ownerId     = dto.ownerId,
        requesterId = dto.requesterId,
        status      = dto.status,
        requestedAt = TimestampConverter.tsToEpochNullable(dto.requestedAt) ?: 0L,
        respondedAt = TimestampConverter.tsToEpochNullable(dto.respondedAt)
    )

    /* ---------- Domain ⇢ DTO ---------- */
    fun toDto(domain: LessonRequest): LessonRequestDto = LessonRequestDto(
        id          = domain.id,
        lessonId    = domain.lessonId,
        ownerId     = domain.ownerId,
        requesterId = domain.requesterId,
        status      = domain.status,
        requestedAt = TimestampConverter.epochToTsNullable(domain.requestedAt),
        respondedAt = TimestampConverter.epochToTsNullable(domain.respondedAt)
    )


    /* ---------- DTO ⇢ Entity ---------- */
    fun toEntity(dto: LessonRequestDto): LessonRequestEntity {
        require(dto.id.isNotBlank()) { "LessonRequestDto.id is blank – cannot insert into Room" }

        return LessonRequestEntity(
            id          = dto.id,
            lessonId    = dto.lessonId,
            ownerId     = dto.ownerId,
            requesterId = dto.requesterId,
            status      = dto.status.name,
            requestedAt = TimestampConverter.tsToDateNullable(dto.requestedAt),
            respondedAt = TimestampConverter.tsToDateNullable(dto.respondedAt)
        )
    }


    /* ---------- Entity ⇢ DTO ---------- */
    fun toDto(entity: LessonRequestEntity): LessonRequestDto = LessonRequestDto(
        id          = entity.id,
        lessonId    = entity.lessonId,
        ownerId     = entity.ownerId,
        requesterId = entity.requesterId,
        status      = RequestStatus.valueOf(entity.status),  // 🔑 String → enum
        requestedAt = TimestampConverter.epochToTsNullable(
            TimestampConverter.toEpochNullable(entity.requestedAt)),
        respondedAt = TimestampConverter.epochToTsNullable(
            TimestampConverter.toEpochNullable(entity.respondedAt))
    )


    /* ---------- Helpers for MapStruct ---------- */
    @Named("stringToStatus")
    fun stringToStatus(s: String): RequestStatus = RequestStatus.valueOf(s)

    @Named("statusToString")
    fun statusToString(s: RequestStatus): String  = s.name
}

/* LessonMapper.kt */
package com.example.exchangingprivatelessons.data.mapper

import com.example.exchangingprivatelessons.common.util.TimestampConverter
import com.example.exchangingprivatelessons.data.local.entity.LessonEntity
import com.example.exchangingprivatelessons.data.remote.dto.LessonDto
import com.example.exchangingprivatelessons.domain.model.Lesson
import com.example.exchangingprivatelessons.domain.model.LessonStatus
import org.mapstruct.*

/**
 * Converts between
 * * Firestore DTO ([LessonDto])
 * * Room entity ([LessonEntity])
 * * Domain model ([Lesson])
 *
 * **Field mapping rules**
 * * `createdAt`      ⇆ `createdAt`   (Long ↔ Date ↔ Timestamp)
 * * `lastUpdatedAt` ⇆ `lastUpdatedAt`
 * * `status`        ⇆ `LessonStatus`
 */
@Mapper(
    componentModel = "kotlin",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = [TimestampConverter::class],
    builder = Builder(disableBuilder = true)
)
abstract class LessonMapper {

    /* ---------- Entity → Domain ---------- */
    @Mappings(
        Mapping(source = "createdAt",     target = "createdAt",
            qualifiedByName = ["toEpochNullable"]),
        Mapping(source = "lastUpdatedAt", target = "lastUpdatedAt",
            qualifiedByName = ["toEpochNullable"]),
        Mapping(source = "status",        target = "status",
            qualifiedByName = ["stringToLessonStatus"])
    )
    abstract fun toDomain(entity: LessonEntity): Lesson

    /* ---------- Domain → Entity ---------- */
    @Mappings(
        Mapping(source = "createdAt",     target = "createdAt",
            qualifiedByName = ["toDateNonNull"]),
        Mapping(source = "lastUpdatedAt", target = "lastUpdatedAt",
            qualifiedByName = ["toDateNonNull"]),
        Mapping(source = "status",        target = "status",
            qualifiedByName = ["lessonStatusToString"])
    )
    abstract fun toEntity(domain: Lesson): LessonEntity

    /* ---------- DTO → Domain ---------- */
    @Mappings(
        Mapping(source = "createdAt",     target = "createdAt",
            qualifiedByName = ["tsToEpochNonNull"]),
        Mapping(source = "lastUpdatedAt", target = "lastUpdatedAt",
            qualifiedByName = ["tsToEpochNonNull"])
    )
    abstract fun toDomain(dto: LessonDto): Lesson

    /* ---------- DTO → Entity ---------- */
    @Mappings(
        Mapping(source = "createdAt",     target = "createdAt",
            qualifiedByName = ["tsToDateNullable"]),
        Mapping(source = "lastUpdatedAt", target = "lastUpdatedAt",
            qualifiedByName = ["tsToDateNullable"]),
        Mapping(source = "status",        target = "status",
            qualifiedByName = ["lessonStatusToString"])
    )
    abstract fun toEntity(dto: LessonDto): LessonEntity

    /* ---------- After‑mapping validation ---------- */
    @AfterMapping
    protected fun validateId(dto: LessonDto, @MappingTarget entity: LessonEntity) {
        require(dto.id.isNotBlank()) { "LessonDto.id is blank – cannot insert into Room" }
    }

    /* ---------- Domain → DTO (manual) ---------- */
    fun toDto(domain: Lesson): LessonDto = LessonDto(
        id            = domain.id,
        ownerId       = domain.ownerId,
        title         = domain.title,
        description   = domain.description,
        status        = domain.status,
        ratingSum     = domain.ratingSum,
        ratingCount   = domain.ratingCount,
        createdAt     = TimestampConverter.epochToTsNullable(domain.createdAt),
        lastUpdatedAt = TimestampConverter.epochToTsNullable(domain.lastUpdatedAt)
    )

    /* ---------- Entity → DTO (manual) ---------- */
    fun toDto(entity: LessonEntity): LessonDto = LessonDto(
        id            = entity.id,
        ownerId       = entity.ownerId,
        title         = entity.title,
        description   = entity.description,
        status        = stringToLessonStatus(entity.status),
        ratingSum     = entity.ratingSum,
        ratingCount   = entity.ratingCount,
        createdAt     = TimestampConverter.dateToTsNullable(entity.createdAt),
        lastUpdatedAt = TimestampConverter.dateToTsNullable(entity.lastUpdatedAt)
    )

    /* ---------- helpers ---------- */
    @Named("stringToLessonStatus")
    fun stringToLessonStatus(s: String): LessonStatus =
        if (s.equals("archived", ignoreCase = true)) LessonStatus.Archived
        else LessonStatus.Active

    @Named("lessonStatusToString")
    fun lessonStatusToString(status: LessonStatus): String = status.name
}

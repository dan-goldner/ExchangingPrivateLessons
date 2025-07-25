package com.example.exchangingprivatelessons.data.mapper

import com.example.exchangingprivatelessons.common.util.TimestampConverter
import com.example.exchangingprivatelessons.data.local.entity.LessonEntity
import com.example.exchangingprivatelessons.data.remote.dto.LessonDto
import com.example.exchangingprivatelessons.domain.model.Lesson
import com.example.exchangingprivatelessons.domain.model.LessonStatus
import org.mapstruct.*

@Mapper(
    componentModel = "kotlin",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = [TimestampConverter::class],
    builder = Builder(disableBuilder = true)
)
interface LessonMapper {

    /* ---------- Entity ↔ Domain (Date ↔ Long, String ↔ LessonStatus) ---------- */
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = ["toEpochNonNull"])
    @Mapping(source = "lastUpdated", target = "lastUpdated", qualifiedByName = ["toEpochNonNull"])
    @Mapping(source = "status", target = "status", qualifiedByName = ["stringToLessonStatus"])
    fun toDomain(entity: LessonEntity): Lesson

    @Mapping(source = "ownerId", target = "ownerId")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = ["toDateNonNull"])
    @Mapping(source = "lastUpdated", target = "lastUpdated", qualifiedByName = ["toDateNonNull"])
    @Mapping(source = "status", target = "status", qualifiedByName = ["lessonStatusToString"])
    fun toEntity(domain: Lesson): LessonEntity

    /* ---------- DTO ➜ Domain (Timestamp ↔ Long) ---------- */
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = ["tsToEpochNonNull"])
    @Mapping(source = "lastUpdated", target = "lastUpdated", qualifiedByName = ["tsToEpochNonNull"])
    fun toDomain(dto: LessonDto): Lesson

    /* ---------- Domain ➜ DTO  — ידני, ללא @Mapping ---------- */
    fun toDto(domain: Lesson): LessonDto = LessonDto(
        id           = domain.id,
        ownerId      = domain.ownerId,
        title        = domain.title,
        description  = domain.description,
        status       = domain.status,
        ratingSum    = domain.ratingSum,
        ratingCount  = domain.ratingCount,
        createdAt    = TimestampConverter.epochToTsNullable(domain.createdAt),
        lastUpdated  = TimestampConverter.epochToTsNullable(domain.lastUpdated)
    )

    /* ---------- DTO ↔ Entity ---------- */
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = ["tsToDateNullable"])
    @Mapping(source = "lastUpdated", target = "lastUpdated", qualifiedByName = ["tsToDateNullable"])
    @Mapping(source = "status", target = "status", qualifiedByName = ["lessonStatusToString"])
    fun toEntity(dto: LessonDto): LessonEntity

    /* Entity ➜ DTO – ידני */
    fun toDto(entity: LessonEntity): LessonDto = LessonDto(
        id           = entity.id,
        ownerId      = entity.ownerId,
        title        = entity.title,
        description  = entity.description,
        status       = stringToLessonStatus(entity.status),
        ratingSum    = entity.ratingSum,
        ratingCount  = entity.ratingCount,
        createdAt    = TimestampConverter.dateToTsNullable(entity.createdAt),
        lastUpdated  = TimestampConverter.dateToTsNullable(entity.lastUpdated)
    )

    /* ---------- Status Conversion Methods ---------- */
    @Named("stringToLessonStatus")
    fun stringToLessonStatus(status: String): LessonStatus {
        return when (status.lowercase()) {
            "active" -> LessonStatus.Active
            "archived" -> LessonStatus.Archived
            else -> LessonStatus.Active // Default fallback
        }
    }

    @Named("lessonStatusToString")
    fun lessonStatusToString(status: LessonStatus): String {
        return when (status) {
            LessonStatus.Active -> "Active"
            LessonStatus.Archived -> "Archived"
        }
    }
}
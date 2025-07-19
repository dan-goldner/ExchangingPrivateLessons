package com.example.exchangingprivatelessons.data.mapper

import com.example.exchangingprivatelessons.common.util.TimestampConverter
import com.example.exchangingprivatelessons.data.local.entity.LessonEntity
import com.example.exchangingprivatelessons.data.remote.dto.LessonDto
import com.example.exchangingprivatelessons.domain.model.Lesson
import org.mapstruct.*

@Mapper(
    componentModel = "kotlin",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = [TimestampConverter::class]
)
interface LessonMapper {

    /* Entity ➜ Domain */
    @Mappings(
        Mapping(source = "createdAt",   target = "createdAt",   qualifiedByName = ["toEpochNonNull"]),
        Mapping(source = "lastUpdated", target = "lastUpdated", qualifiedByName = ["toEpochNonNull"])
    )
    fun toDomain(entity: LessonEntity): Lesson

    /* Domain ➜ Entity */
    @Mappings(
        Mapping(source = "createdAt",   target = "createdAt",   qualifiedByName = ["toDateNonNull"]),
        Mapping(source = "lastUpdated", target = "lastUpdated", qualifiedByName = ["toDateNonNull"])
    )
    fun toEntity(domain: Lesson): LessonEntity


    /* DTO ↔ Domain (Long ↔ Long, בלי אנוטציות) */
    fun toDomain(dto: LessonDto): Lesson
    fun toDto(domain: Lesson): LessonDto


    /* DTO ↔ Entity */
    @Mappings(
        Mapping(source = "createdAt",   target = "createdAt",   qualifiedByName = ["toDateNonNull"]),
        Mapping(source = "lastUpdated", target = "lastUpdated", qualifiedByName = ["toDateNonNull"])
    )
    fun toEntity(dto: LessonDto): LessonEntity

    @Mappings(
        Mapping(source = "createdAt",   target = "createdAt",   qualifiedByName = ["toEpochNonNull"]),
        Mapping(source = "lastUpdated", target = "lastUpdated", qualifiedByName = ["toEpochNonNull"])
    )
    fun toDto(entity: LessonEntity): LessonDto
}

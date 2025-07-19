package com.example.exchangingprivatelessons.data.mapper

import com.example.exchangingprivatelessons.common.util.TimestampConverter
import com.example.exchangingprivatelessons.data.local.entity.TakenLessonEntity
import com.example.exchangingprivatelessons.data.remote.dto.TakenLessonDto
import com.example.exchangingprivatelessons.domain.model.TakenLesson
import org.mapstruct.*

@Mapper(
    componentModel = "kotlin",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = [TimestampConverter::class, LessonJsonConverter::class]
)
interface TakenLessonMapper {

    /* ---------- Entity ⇄ Domain ---------- */

    @Mapping(
        target = "lesson",
        source = "lessonJson",
        qualifiedByName = ["jsonToLesson"]
    )
    fun toDomain(entity: TakenLessonEntity): TakenLesson

    @Mapping(
        target = "lessonJson",
        source = "lesson",
        qualifiedByName = ["lessonToJson"]
    )
    fun toEntity(domain: TakenLesson): TakenLessonEntity


    /* ---------- DTO ⇄ Domain (Long↔Long ‑ אין המרות) ---------- */
    fun dtoToDomain(dto: TakenLessonDto): TakenLesson
    fun domainToDto(domain: TakenLesson): TakenLessonDto


    /* ---------- DTO ⇄ Entity ---------- */

    @Mapping(
        target = "lessonJson",
        source = "lesson",
        qualifiedByName = ["lessonDtoToJson"]
    )
    fun dtoToEntity(dto: TakenLessonDto): TakenLessonEntity

    @Mapping(
        target = "lesson",
        source = "lessonJson",
        qualifiedByName = ["jsonToLessonDto"]
    )
    fun entityToDto(entity: TakenLessonEntity): TakenLessonDto
}

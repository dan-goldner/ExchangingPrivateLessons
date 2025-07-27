/* ───────────  TakenLessonMapper  ─────────── */
package com.example.exchangingprivatelessons.data.mapper

import com.example.exchangingprivatelessons.common.util.TimestampConverter
import com.example.exchangingprivatelessons.data.local.entity.TakenLessonEntity
import com.example.exchangingprivatelessons.data.remote.dto.LessonDto
import com.example.exchangingprivatelessons.data.remote.dto.TakenLessonDto
import com.example.exchangingprivatelessons.domain.model.Lesson
import com.example.exchangingprivatelessons.domain.model.TakenLesson
import com.google.gson.Gson
import org.mapstruct.*
import org.mapstruct.factory.Mappers

/* ───────────  TakenLessonMapper  ─────────── */
@Mapper(
    componentModel   = "kotlin",
    uses             = [TimestampConverter::class, LessonMapper::class],
    builder          = Builder(disableBuilder = true)
)
abstract class TakenLessonMapper {

    /* ---------- Entity ⇄ Domain ---------- */

    @Mappings(
        Mapping(source = "lessonJson",
            target  = "lesson",
            qualifiedByName = ["jsonToLesson"]),
        Mapping(source = "ownerName",      target = "ownerName"),
        Mapping(source = "ownerPhotoUrl",  target = "ownerPhotoUrl"),
        Mapping(source = "canRate",        target = "canRate"),
        Mapping(source = "takenAt",        target = "takenAt")
    )
    abstract fun toDomain(entity: TakenLessonEntity): TakenLesson

    @InheritInverseConfiguration
    @Mappings(
        Mapping(source = "lesson",
            target  = "lessonJson",
            qualifiedByName = ["lessonToJson"])
    )
    abstract fun toEntity(domain: TakenLesson): TakenLessonEntity


    /* ---------- DTO ⇄ Entity (לבצע upsert מה‑listener) ---------- */

    fun dtoToEntity(dto: TakenLessonDto): TakenLessonEntity = TakenLessonEntity(
        lessonId       = dto.lessonId,
        lessonJson     = dto.lesson?.let { lessonDtoToJson(it) } ?: "{}",
        ownerName      = dto.ownerName ?: "",
        ownerPhotoUrl  = dto.ownerPhotoUrl,
        canRate        = dto.canRate,
        takenAt        = TimestampConverter.tsToEpochNullable(dto.takenAt)
    )


    /* ---------- Named helpers ---------- */

    companion object {
        private val gson          = Gson()
        private val lessonMapper  = Mappers.getMapper(LessonMapper::class.java)

        @JvmStatic @Named("jsonToLesson")
        fun jsonToLesson(json: String): Lesson =
            lessonMapper.toDomain(gson.fromJson(json, LessonDto::class.java))

        @JvmStatic @Named("lessonToJson")
        fun lessonToJson(lesson: Lesson): String =
            gson.toJson(lessonMapper.toDto(lesson))

        @JvmStatic @Named("lessonDtoToJson")
        fun lessonDtoToJson(dto: LessonDto): String = gson.toJson(dto)
    }
}

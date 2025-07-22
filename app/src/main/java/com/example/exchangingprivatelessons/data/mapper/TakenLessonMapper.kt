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

@Mapper(
    componentModel   = "kotlin",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = [TimestampConverter::class, LessonMapper::class],
    builder = Builder(disableBuilder = true)
)
abstract class TakenLessonMapper {

    /* ------------------------------------------------------------------ */
    /*                        Entity  ↔  Domain                            */
    /* ------------------------------------------------------------------ */

    @Mappings(
        Mapping(source = "lessonJson",
            target = "lesson",
            qualifiedByName = ["jsonToLesson"]),
        Mapping(source = "takenAt", target = "takenAt")          // Long↔Long
    )
    abstract fun toDomain(entity: TakenLessonEntity): TakenLesson

    @Mappings(
        Mapping(source = "lesson",
            target = "lessonJson",
            qualifiedByName = ["lessonToJson"]),
        Mapping(source = "takenAt", target = "takenAt")
    )
    abstract fun toEntity(domain: TakenLesson): TakenLessonEntity



    /* ------------------------------------------------------------------ */
    /*                        DTO  ↔  Domain                               */
    /* ------------------------------------------------------------------ */

    @Mappings(
        Mapping(source = "lesson",
            target = "lesson"),                               // LessonDto → Lesson (LessonMapper)
        Mapping(source = "takenAt",
            target = "takenAt",
            qualifiedByName = ["tsToEpochNullable"])
    )
    abstract fun dtoToDomain(dto: TakenLessonDto): TakenLesson

    @Mappings(
        Mapping(source = "lesson",
            target = "lesson"),                               // Lesson → LessonDto
        Mapping(source = "takenAt",
            target = "takenAt",
            qualifiedByName = ["epochToTsNullable"])
    )
    abstract fun domainToDto(domain: TakenLesson): TakenLessonDto



    /* ------------------------------------------------------------------ */
    /*                        DTO  ↔  Entity                               */
    /* ------------------------------------------------------------------ */

    @Mappings(
        Mapping(source = "lesson",
            target = "lessonJson",
            qualifiedByName = ["lessonDtoToJson"]),
        Mapping(source = "takenAt",
            target = "takenAt",
            qualifiedByName = ["tsToEpochNullable"])
    )
    abstract fun dtoToEntity(dto: TakenLessonDto): TakenLessonEntity

    @Mappings(
        Mapping(source = "lessonJson",
            target = "lesson",
            qualifiedByName = ["jsonToLessonDto"]),
        Mapping(source = "takenAt",
            target = "takenAt",
            qualifiedByName = ["epochToTsNullable"])
    )
    abstract fun entityToDto(entity: TakenLessonEntity): TakenLessonDto



    /* ------------------------------------------------------------------ */
    /*    Helpers – JSON ⇄ Lesson / LessonDto   (used via @Named)         */
    /* ------------------------------------------------------------------ */

    companion object {

        private val gson           = Gson()
        private val lessonMapper   = Mappers.getMapper(LessonMapper::class.java)

        /* ---------- JSON  ↠  Lesson ---------- */
        @JvmStatic
        @Named("jsonToLesson")
        fun jsonToLesson(json: String): Lesson {
            val dto: LessonDto = gson.fromJson(json, LessonDto::class.java)
            return lessonMapper.toDomain(dto)
        }

        /* ---------- Lesson ↠  JSON  ---------- */
        @JvmStatic
        @Named("lessonToJson")
        fun lessonToJson(lesson: Lesson): String {
            val dto = lessonMapper.toDto(lesson)
            return gson.toJson(dto)
        }

        /* ---------- JSON  ↠  LessonDto ---------- */
        @JvmStatic
        @Named("jsonToLessonDto")
        fun jsonToLessonDto(json: String): LessonDto =
            gson.fromJson(json, LessonDto::class.java)

        /* ---------- LessonDto ↠ JSON ---------- */
        @JvmStatic
        @Named("lessonDtoToJson")
        fun lessonDtoToJson(dto: LessonDto): String =
            gson.toJson(dto)
    }
}

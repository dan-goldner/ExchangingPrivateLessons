package com.example.exchangingprivatelessons.data.mapper

import com.example.exchangingprivatelessons.domain.model.Lesson
import com.example.exchangingprivatelessons.data.remote.dto.LessonDto
import kotlinx.serialization.json.Json
import org.mapstruct.Named
import kotlin.jvm.JvmStatic

object LessonJsonConverter {

    /* ---- Domain Lesson ⇄ JSON ---- */

    @JvmStatic @Named("lessonToJson")
    fun lessonToJson(lesson: Lesson): String =
        Json.encodeToString(Lesson.serializer(), lesson)

    @JvmStatic @Named("jsonToLesson")
    fun jsonToLesson(json: String): Lesson =
        Json.decodeFromString(Lesson.serializer(), json)

    /* ---- LessonDto ⇄ JSON ---- */

    @JvmStatic @Named("lessonDtoToJson")
    fun lessonDtoToJson(dto: LessonDto): String =
        Json.encodeToString(LessonDto.serializer(), dto)

    @JvmStatic @Named("jsonToLessonDto")
    fun jsonToLessonDto(json: String): LessonDto =
        Json.decodeFromString(LessonDto.serializer(), json)
}

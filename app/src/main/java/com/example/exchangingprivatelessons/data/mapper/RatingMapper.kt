/* ───────────  RatingMapper  ─────────── */
package com.example.exchangingprivatelessons.data.mapper

import com.example.exchangingprivatelessons.common.util.TimestampConverter
import com.example.exchangingprivatelessons.data.local.entity.RatingEntity
import com.example.exchangingprivatelessons.data.remote.dto.RatingDto
import com.example.exchangingprivatelessons.domain.model.Rating
import org.mapstruct.*

@Mapper(
    componentModel = "kotlin",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses  = [TimestampConverter::class],
    builder = Builder(disableBuilder = true)
)
abstract class RatingMapper {

    /* ----- Entity ⇄ Domain (כבר קיימות) ----- */

    @Mapping(source = "ratedAt", target = "ratedAt",
        qualifiedByName = ["toEpochNullable"])
    abstract fun toDomain(entity: RatingEntity): Rating

    @Mapping(source = "ratedAt", target = "ratedAt",
        qualifiedByName = ["toDateNullable"])
    abstract fun toEntity(domain: Rating): RatingEntity


    /* ----- DTO ⇄ Domain  (ללא lessonId / userId) ----- */
    fun toDomain(dto: RatingDto): Rating = Rating(
        lessonId      = dto.lessonId,
        userId        = dto.uid,
        numericValue  = dto.numericValue,
        comment       = dto.comment,
        ratedAt       = dto.ratedAt ?: 0L
    )

    /* ----- DTO ⇄ Entity  (Date? ←→ Long?) ----- */
    fun toEntity(dto: RatingDto): RatingEntity = RatingEntity(
        lessonId      = dto.lessonId,
        userId        = dto.uid,
        numericValue  = dto.numericValue,
        comment       = dto.comment,
        ratedAt       = TimestampConverter.toDateNullable(dto.ratedAt)
    )
}

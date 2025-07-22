/* ─────────── RatingMapper (Fixed) ─────────── */
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

    /* ---------- Entity ↔ Domain ---------- */

    @Mapping(source = "ratedAt", target = "ratedAt",
        qualifiedByName = ["toEpochNullable"])
    abstract fun entityToDomain(entity: RatingEntity): Rating

    // Alias for repository compatibility
    fun toDomain(entity: RatingEntity): Rating = entityToDomain(entity)

    @Mapping(source = "ratedAt", target = "ratedAt",
        qualifiedByName = ["toDateNullable"])
    abstract fun domainToEntity(domain: Rating): RatingEntity

    /* ---------- DTO ↔ Domain ---------- */

    fun dtoToDomain(dto: RatingDto): Rating = Rating(
        lessonId     = dto.lessonId,
        userId       = dto.uid,
        numericValue = dto.numericValue,
        comment      = dto.comment,
        ratedAt      = TimestampConverter.tsToEpochNullable(dto.ratedAt) ?: 0L
    )

    // Alias for repository compatibility
    fun toDomain(dto: RatingDto): Rating = dtoToDomain(dto)

    fun domainToDto(domain: Rating): RatingDto = RatingDto(
        lessonId     = domain.lessonId,
        uid          = domain.userId,
        numericValue = domain.numericValue,
        comment      = domain.comment,
        ratedAt      = TimestampConverter.epochToTsNullable(domain.ratedAt)
    )

    /* ---------- DTO ↔ Entity ---------- */

    fun dtoToEntity(dto: RatingDto): RatingEntity = RatingEntity(
        lessonId     = dto.lessonId,
        userId       = dto.uid,
        numericValue = dto.numericValue,
        comment      = dto.comment,
        ratedAt      = TimestampConverter.tsToDateNullable(dto.ratedAt)
    )

    // Alias for repository compatibility
    fun toEntity(dto: RatingDto): RatingEntity = dtoToEntity(dto)

    fun entityToDto(entity: RatingEntity): RatingDto = RatingDto(
        lessonId     = entity.lessonId,
        uid          = entity.userId,
        numericValue = entity.numericValue,
        comment      = entity.comment,
        ratedAt      = TimestampConverter.epochToTsNullable(
            TimestampConverter.toEpochNullable(entity.ratedAt))
    )
}
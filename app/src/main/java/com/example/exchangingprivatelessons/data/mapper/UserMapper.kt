/* ───────────  UserMapper  ─────────── */
package com.example.exchangingprivatelessons.data.mapper

import com.example.exchangingprivatelessons.common.util.TimestampConverter
import com.example.exchangingprivatelessons.common.util.millis          // ext  Timestamp→epoch ms
import com.example.exchangingprivatelessons.data.local.entity.UserEntity
import com.example.exchangingprivatelessons.data.remote.dto.UserDto
import com.example.exchangingprivatelessons.domain.model.User
import org.mapstruct.*

@Mapper(
    componentModel    = "kotlin",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses              = [TimestampConverter::class],
    builder           = Builder(disableBuilder = true)
)
abstract class UserMapper {

    /* ---------- Entity → Domain ---------- */
    fun toDomain(entity: UserEntity) = with(entity) {
        User(
            uid          = id,
            displayName  = displayName,
            email        = email,
            photoUrl     = photoUrl,
            bio          = bio,
            score        = score,
            createdAt    = createdAt?.time    ?: 0L,
            lastLoginAt  = lastLoginAt?.time  ?: 0L,
            lastUpdated  = lastUpdated?.time
        )
    }

    /* ---------- Domain → Entity ---------- */
    @Mappings(
        Mapping(source = "uid",         target = "id"),
        Mapping(source = "createdAt",   target = "createdAt",   qualifiedByName = ["toDateNullable"]),
        Mapping(source = "lastLoginAt", target = "lastLoginAt", qualifiedByName = ["toDateNullable"]),
        Mapping(source = "lastUpdated", target = "lastUpdated", qualifiedByName = ["toDateNullable"])
    )
    abstract fun toEntity(domain: User): UserEntity

    /* ---------- DTO → Domain ---------- */
    fun toDomain(dto: UserDto): User = User(
        uid          = dto.id,
        displayName  = dto.displayName,
        email        = dto.email,
        photoUrl     = dto.photoUrl,
        bio          = dto.bio,
        score        = dto.score,
        createdAt    = dto.createdAt   ?.millis ?: 0L,
        lastLoginAt  = dto.lastLoginAt ?.millis ?: 0L,
        lastUpdated  = dto.lastUpdatedAt?.millis
    )

    /* ---------- DTO → Entity ---------- */
    fun toEntity(dto: UserDto) = UserEntity(
        id          = dto.id,
        displayName = dto.displayName,
        email       = dto.email,
        photoUrl    = dto.photoUrl,
        bio         = dto.bio,
        score       = dto.score,
        createdAt   = TimestampConverter.tsToDateNullable(dto.createdAt),
        lastLoginAt = TimestampConverter.tsToDateNullable(dto.lastLoginAt),
        lastUpdated = TimestampConverter.tsToDateNullable(dto.lastUpdatedAt)
    )

    /* ---------- Domain → DTO ---------- */
    fun toDto(domain: User): UserDto = UserDto(
        id            = domain.uid,
        displayName   = domain.displayName,
        email         = domain.email,
        photoUrl      = domain.photoUrl,
        bio           = domain.bio,
        score         = domain.score,
        createdAt     = TimestampConverter.epochToTsNullable(domain.createdAt),
        lastLoginAt   = TimestampConverter.epochToTsNullable(domain.lastLoginAt),
        lastUpdatedAt = TimestampConverter.epochToTsNullable(domain.lastUpdated)
    )

    /* ---------- Entity → DTO ---------- */
    fun toDto(entity: UserEntity): UserDto = UserDto(
        id            = entity.id,
        displayName   = entity.displayName,
        email         = entity.email,
        photoUrl      = entity.photoUrl,
        bio           = entity.bio,
        score         = entity.score,
        createdAt     = TimestampConverter.epochToTsNullable(
            TimestampConverter.toEpochNullable(entity.createdAt)
        ),
        lastLoginAt   = TimestampConverter.epochToTsNullable(
            TimestampConverter.toEpochNullable(entity.lastLoginAt)
        ),
        lastUpdatedAt = TimestampConverter.epochToTsNullable(
            TimestampConverter.toEpochNullable(entity.lastUpdated)
        )
    )
}

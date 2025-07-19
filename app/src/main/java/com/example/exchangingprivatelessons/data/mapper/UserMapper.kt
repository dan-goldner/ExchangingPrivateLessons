/* ───────────  UserMapper  ─────────── */
package com.example.exchangingprivatelessons.data.mapper

import com.example.exchangingprivatelessons.common.util.TimestampConverter
import com.example.exchangingprivatelessons.data.local.entity.UserEntity
import com.example.exchangingprivatelessons.data.remote.dto.UserDto
import com.example.exchangingprivatelessons.domain.model.User
import org.mapstruct.*

@Mapper(
    componentModel = "kotlin",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = [TimestampConverter::class],
    builder = Builder(disableBuilder = true)
)
abstract class UserMapper {

    /* ---------- Entity ⇢ Domain ---------- */
    @Mappings(
        Mapping(source = "id",          target = "uid"),
        Mapping(source = "createdAt",   target = "createdAt",   qualifiedByName = ["toEpochNullable"]),
        Mapping(source = "lastLoginAt", target = "lastLoginAt", qualifiedByName = ["toEpochNullable"]),
        Mapping(target = "lastUpdated", ignore = true)          // ל‑Domain אין כרגע field תואם
    )
    abstract fun toDomain(entity: UserEntity): User

    /* ---------- Domain ⇢ Entity ---------- */
    @Mappings(
        Mapping(source = "uid",         target = "id"),
        Mapping(source = "createdAt",   target = "createdAt",   qualifiedByName = ["toDateNullable"]),
        Mapping(source = "lastLoginAt", target = "lastLoginAt", qualifiedByName = ["toDateNullable"]),
        Mapping(source = "lastUpdated", target = "lastUpdated", qualifiedByName = ["toDateNullable"])
    )
    abstract fun toEntity(domain: User): UserEntity


    /* === המרות   DTO ⇄ Entity / Domain – ידני, כדי להימנע מ‑val‑setter errors === */

    /** DTO → Domain (אין lastUpdated ב‑DTO) */
    fun toDomain(dto: UserDto): User = User(
        uid          = dto.id,
        displayName  = dto.displayName,
        email        = dto.email,
        photoUrl     = dto.photoUrl,
        bio          = dto.bio,
        score        = dto.score,
        createdAt    = dto.createdAt ?: 0L,
        lastLoginAt  = dto.lastLoginAt ?: 0L,
        lastUpdated  = null
    )

    /** DTO → Entity */
    fun toEntity(dto: UserDto): UserEntity = UserEntity(
        id           = dto.id,
        displayName  = dto.displayName,
        email        = dto.email,
        photoUrl     = dto.photoUrl,
        bio          = dto.bio,
        score        = dto.score,
        createdAt    = TimestampConverter.toDateNullable(dto.createdAt),
        lastLoginAt  = TimestampConverter.toDateNullable(dto.lastLoginAt),
        lastUpdated  = null
    )

    /** Domain → DTO */
    fun toDto(domain: User): UserDto = UserDto(
        id           = domain.uid,
        displayName  = domain.displayName,
        email        = domain.email,
        photoUrl     = domain.photoUrl,
        bio          = domain.bio,
        score        = domain.score,
        createdAt    = domain.createdAt,
        lastLoginAt  = domain.lastLoginAt
    )

    /** Entity → DTO */
    fun toDto(entity: UserEntity): UserDto = UserDto(
        id           = entity.id,
        displayName  = entity.displayName,
        email        = entity.email,
        photoUrl     = entity.photoUrl,
        bio          = entity.bio,
        score        = entity.score,
        createdAt    = TimestampConverter.toEpochNullable(entity.createdAt),
        lastLoginAt  = TimestampConverter.toEpochNullable(entity.lastLoginAt)
    )
}

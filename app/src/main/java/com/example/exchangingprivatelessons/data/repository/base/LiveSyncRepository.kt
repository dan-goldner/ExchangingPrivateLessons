package com.example.exchangingprivatelessons.data.repository.base

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.common.util.mapList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

abstract class LiveSyncRepository<TDto, TEntity, TDomain> {

    protected abstract fun listenRemote(): Flow<Result<List<TDto>>>
    protected abstract suspend fun replaceAllLocal(list: List<TEntity>)
    protected abstract fun toEntity(dto: TDto): TEntity
    protected abstract fun toDomain(entity: TEntity): TDomain

    fun observe(): Flow<Result<List<TDomain>>> =
        listenRemote().map { res ->
            if (res is Result.Success) {
                replaceAllLocal(res.data.map(::toEntity))
            }
            res.mapList { dto -> toDomain(toEntity(dto)) }
        }
}

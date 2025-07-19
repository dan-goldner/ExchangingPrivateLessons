package com.example.exchangingprivatelessons.data.repository.base

import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.common.util.mapList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * TLocal  – Room entity
 * TRemote – DTO from the server
 * TOut    – Domain model (UI layer)
 */
abstract class NetworkCacheRepository<TLocal, TRemote, TOut>(
    private val io: CoroutineDispatcher
) {

    protected abstract fun queryLocal(): Flow<List<TLocal>>
    protected abstract suspend fun fetchRemote(): List<TRemote>
    protected abstract suspend fun saveRemote(list: List<TRemote>)
    protected abstract fun map(local: TLocal): TOut

    protected open fun shouldFetch(cached: List<TLocal>): Boolean = true

    fun observe(): Flow<Result<List<TOut>>> =
        networkBoundResource(
            io = io,
            query = ::queryLocal,
            fetch = ::fetchRemote,
            saveFetchResult = ::saveRemote,
            shouldFetch = ::shouldFetch
        ).map { result ->
            result.mapList(::map)
        }
}

package com.example.exchangingprivatelessons.data.repository.base

import com.example.exchangingprivatelessons.common.util.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Standard *network‑bound* flow:
 *
 * 1. Emit Loading
 * 2. Emit cached data (if any)
 * 3. Optionally fetch remote
 * 4. Persist → emit updated cache
 * 5. On remote failure emit Result.Failure **in parallel** to the last Success
 */
inline fun <Local, Remote> networkBoundResource(
    io: CoroutineDispatcher,
    crossinline query: () -> Flow<List<Local>>,
    crossinline fetch: suspend () -> List<Remote>,
    crossinline saveFetchResult: suspend (List<Remote>) -> Unit,
    crossinline shouldFetch: (List<Local>) -> Boolean = { true }
): Flow<Result<List<Local>>> = channelFlow {

    // 1. Loading
    send(Result.Loading)

    // 2. Cache stream
    val cacheJob = query()
        .map { Result.Success(it) as Result<List<Local>> }
        .onEach { send(it) }
        .launchIn(this)

    // 3‑4. Remote refresh
    launch(io) {
        try {
            val current = query().first()
            if (!shouldFetch(current)) return@launch

            val remote = fetch()
            saveFetchResult(remote)
        } catch (t: Throwable) {
            // 5. Surface the error
            send(Result.Failure(t))
        }
    }

    awaitClose { cacheJob.cancel() }
}

package com.example.exchangingprivatelessons.common.util

/**
 * Unified wrapper for sync calls *and* Flow‑streams.
 *
 *  ▪ Success   – contains the data
 *  ▪ Failure   – contains the Throwable
 *  ▪ Loading   – use when you want to expose a “work in progress”
 */
sealed interface Result<out T> {

    data class Success<out T>(val data: T) : Result<T>
    data class Failure(val throwable: Throwable) : Result<Nothing>
    object Loading : Result<Nothing>

    /* ---------- in‑line helpers ---------- */

}

inline fun <T> Result<T>.onFailure(block: (Throwable) -> Unit): Result<T> =
    also { if (this is Result.Failure) block(throwable) }

inline fun <T> Result<T>.onSuccess(block: (T) -> Unit): Result<T> =
    also { if (this is Result.Success) block(data) }

/* ---------- extension map ---------- */

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Failure -> this
    is Result.Loading -> Result.Loading
}

inline fun <T, R> Result<List<T>>.mapList(transform: (T) -> R): Result<List<R>> = when (this) {
    is Result.Success -> Result.Success(data.map(transform))
    is Result.Failure -> this
    is Result.Loading -> Result.Loading
}

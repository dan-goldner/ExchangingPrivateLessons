package com.example.exchangingprivatelessons.common.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Flow<Result<T>> ➜ LiveData<Result<T>>
 * @param context  CoroutineContext שבו יתבצע האיסוף (ברירת‑מחדל = Main.immediate)
 */
fun <T> Flow<Result<T>>.toResultLiveData(
    context: CoroutineContext = EmptyCoroutineContext
): LiveData<Result<T>> =
    onStart { emit(Result.Loading) }.asLiveData(context)

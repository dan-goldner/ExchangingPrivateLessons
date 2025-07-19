package com.example.exchangingprivatelessons.common.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import com.example.exchangingprivatelessons.common.util.Result

/** הופך Flow<Result<T>> → LiveData<Result<T>>, ומזריק Result.Loading בתחילת הזרם. */
fun <T> Flow<Result<T>>.toResultLiveData() : LiveData<Result<T>> =
    onStart { emit(Result.Loading) }.asLiveData()

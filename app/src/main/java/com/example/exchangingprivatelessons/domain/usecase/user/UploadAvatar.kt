package com.example.exchangingprivatelessons.domain.usecase.user

import android.net.Uri
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.data.remote.cloud.StorageDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadAvatar @Inject constructor(
    private val storage: StorageDataSource
) {
    suspend operator fun invoke(uid: String, file: Uri): Result<String> = try {
        Result.Success(storage.uploadUserAvatar(uid, file))
    } catch (t: Throwable) {
        Result.Failure(t)
    }
}

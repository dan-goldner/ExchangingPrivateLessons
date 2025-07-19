package com.example.exchangingprivatelessons.data.remote.cloud

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageDataSource @Inject constructor(
    private val storage: FirebaseStorage
) {

    /** מעלה קובץ (תמונת שיעור / פרופיל) ומחזיר URL ציבורי */
    suspend fun upload(path: String, fileUri: Uri): String {
        val ref = storage.reference.child(path)
        ref.putFile(fileUri).await()
        return ref.downloadUrl.await().toString()
    }

    /** מוחק קובץ אם קיים */
    suspend fun delete(path: String) = runCatching {
        storage.reference.child(path).delete().await()
    }.getOrNull()
}

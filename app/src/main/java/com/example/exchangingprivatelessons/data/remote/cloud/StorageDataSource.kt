/* data/remote/storage/StorageDataSource.kt */
package com.example.exchangingprivatelessons.data.remote.storage

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageDataSource @Inject constructor(
    private val storage: FirebaseStorage
) {

    /** מעלה את ה‑Avatar ומחזיר Download‑URL */
    suspend fun uploadAvatar(uid: String, file: Uri): String {
        val ref = storage.reference.child("avatars/$uid.jpg")
        ref.putFile(file).await()               // upload
        return ref.downloadUrl.await().toString()
    }

    /** מחיקת תמונה ישנה (לא חובה אך מומלץ) */
    suspend fun deleteAvatar(uid: String) {
        runCatching { storage.reference.child("avatars/$uid.jpg").delete().await() }
    }
}

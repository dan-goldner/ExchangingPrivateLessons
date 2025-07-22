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

    /* ───────────  generic (private) ─────────── */

    private suspend fun upload(path: String, local: Uri): String {
        val ref = storage.reference.child(path)
        ref.putFile(local).await()
        return ref.downloadUrl.await().toString()
    }

    /* ───────────  public helpers  ─────────── */

    /** ‎`avatars/{uid}.jpg` – תמונה אחת מסודרת למשתמש */
    suspend fun uploadUserAvatar(uid: String, local: Uri): String =
        upload("avatars/$uid.jpg", local)

    /** מחיקת קובץ (אם קיים) – אופציונלי */
    suspend fun delete(path: String) = runCatching {
        storage.reference.child(path).delete().await()
    }.getOrNull()
}

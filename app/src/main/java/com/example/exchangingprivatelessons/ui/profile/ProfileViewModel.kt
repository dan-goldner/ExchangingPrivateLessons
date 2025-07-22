package com.example.exchangingprivatelessons.ui.profile

import android.content.Context
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.lifecycle.*
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.common.util.toResultLiveData
import com.example.exchangingprivatelessons.domain.model.User
import com.example.exchangingprivatelessons.domain.usecase.user.*
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val observeUser   : ObserveUser,
    private val updateProfile : UpdateProfile,
    private val uploadAvatar  : UploadAvatar,
    private val deleteAccount : DeleteAccount
) : ViewModel() {

    /* ------------------------------------------------------------------ */
    /* UID של הפרופיל שנצפה                                                 */
    private val _uid = MutableLiveData<String?>()
    val isMine: Boolean get() = _uid.value == null   // null → current user
    fun setProfileUid(uid: String?) { _uid.value = uid }

    /* ------------------------------------------------------------------ */
    /* ------------------------------------------------------------------ */
    /* User stream */
    val user: LiveData<User?> =
        _uid.switchMap { observeUser(it).toResultLiveData() }  // it יכול להיות null
            .map { (it as? Result.Success)?.data }


    /* ------------------------------------------------------------------ */
    /* Snackbars                                                           */
    private val _snackbar = MutableLiveData<String>()
    val snackbar: LiveData<String> = _snackbar

    /* ------------------------------------------------------------------ */
    /* Avatar preview                                                      */
    private val _previewAvatar = MutableLiveData<Any>()    // Uri OR String(URL) OR ""
    val previewAvatar: LiveData<Any> = _previewAvatar

    var pendingCameraUri: Uri? = null; private set

    fun onNewAvatar(local: Uri) { _previewAvatar.value = local }
    fun onDeleteAvatar()       { _previewAvatar.value = "" }

    fun createTempCameraUri(ctx: Context): Uri {
        val file = File.createTempFile("avatar_", ".jpg", ctx.cacheDir)
        return FileProvider.getUriForFile(
            ctx,
            "${ctx.packageName}.provider",
            file
        ).also { pendingCameraUri = it }
    }

    /* ------------------------------------------------------------------ */
    /* SAVE                                                                */
    fun save(display: String, bio: String) = viewModelScope.launch {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

        val newUrl: String? = when (val v = _previewAvatar.value) {
            is Uri    -> when (val r = uploadAvatar(uid, v)) {
                is Result.Success -> r.data
                is Result.Failure -> { _snackbar.postValue(r.throwable.message); return@launch }
                is Result.Loading -> null            // ← הוספנו ענף Loading
            }
            is String -> v
            else      -> null
        }

        val res = updateProfile(
            displayName = display.ifBlank { null },
            bio         = bio,
            photoUrl    = newUrl
        )

        _snackbar.postValue(
            if (res is Result.Success) "Profile updated"
            else (res as Result.Failure).throwable.message ?: "Error"
        )
    }

    /* ------------------------------------------------------------------ */
    /* Delete account                                                      */
    fun deleteMyAccount() = viewModelScope.launch {
        when (val r = deleteAccount()) {
            is Result.Success -> _snackbar.postValue("Account deleted")
            is Result.Failure -> _snackbar.postValue(r.throwable.message ?: "Delete failed")
            else -> Unit
        }
    }
}

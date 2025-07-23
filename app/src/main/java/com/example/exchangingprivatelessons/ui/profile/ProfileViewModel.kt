package com.example.exchangingprivatelessons.ui.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.*
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.common.util.toResultLiveData
import com.example.exchangingprivatelessons.domain.model.User
import com.example.exchangingprivatelessons.domain.repository.UserRepository
import com.example.exchangingprivatelessons.domain.usecase.user.*
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/** עטיפה לאירוע‑חד‑פעמי  */
class SingleEvent<out T>(private val content: T) {
    private var handled = false
    fun getOrNull(): T? = if (handled) null else { handled = true; content }
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val observeUser  : ObserveUser,
    private val updateProfile: UpdateProfile,
    private val uploadAvatar : UploadAvatar,
    private val deleteAccount: DeleteAccount,
    private val repo         : UserRepository
) : ViewModel() {

    /* ---------- whose profile ---------- */
    private val _uid = MutableLiveData<String?>(null)
    val isMine: Boolean get() = _uid.value == null
    fun setProfileUid(uid: String?) { _uid.value = uid }

    /* ---------- User stream ---------- */
    val user: LiveData<User?> =
        _uid.switchMap { observeUser(it).toResultLiveData() }
            .map { (it as? Result.Success)?.data }

    /* ---------- Snackbar ---------- */
    private val _snackbar = MutableLiveData<String>()
    val snackbar: LiveData<String> = _snackbar

    /* ---------- Avatar preview ---------- */
    private val _previewAvatar = MutableLiveData<Any>("")
    val previewAvatar: LiveData<Any> = _previewAvatar

    fun initPreview(url: String?) {
        if (_previewAvatar.value == "") _previewAvatar.value = url ?: ""
    }
    fun onNewAvatar(uri: Uri) { _previewAvatar.value = uri }
    fun onDeleteAvatar()      { _previewAvatar.value = "" }

    /* ---------- Save profile ---------- */
    fun save(display: String, bio: String) = viewModelScope.launch {
        val current = user.value ?: return@launch
        val v       = _previewAvatar.value

        val newUrl = when (v) {
            is Uri -> when (val r = repo.updateAvatar(v)) {
                is Result.Success -> r.data
                is Result.Failure -> { _snackbar.value = r.throwable.message; return@launch }
                else -> return@launch
            }
            "" -> { if (current.photoUrl.isNotBlank()) repo.removeAvatar(); "" }
            is String -> v
            else -> current.photoUrl
        }

        updateProfile(display.ifBlank { null }, bio, newUrl)
    }

    /* ---------- Camera helper ---------- */
    var pendingCameraUri: Uri? = null; private set
    fun createTempCameraUri(ctx: Context): Uri =
        File.createTempFile("avatar_", ".jpg", ctx.cacheDir)
            .apply { deleteOnExit() }
            .let {
                FileProvider.getUriForFile(
                    ctx, "${ctx.packageName}.fileprovider", it
                ).also { pendingCameraUri = it }
            }

    /* ---------- Sign‑out event ---------- */
    private val _signOut = MutableLiveData<SingleEvent<Unit>>()
    val signOut: LiveData<SingleEvent<Unit>> = _signOut

    /* ---------- Delete‑account ---------- */
    fun deleteMyAccount() = viewModelScope.launch {
        when (val r = deleteAccount()) {
            is Result.Success -> {
                Log.d("ProfileVM", "deleteAccount → Success")
                repo.clearLocalUser()
                FirebaseAuth.getInstance().signOut()
                _signOut.postValue(SingleEvent(Unit))
            }
            is Result.Failure -> {
                Log.e("ProfileVM", "deleteAccount failed", r.throwable)
                _snackbar.value = r.throwable.localizedMessage ?: "Delete failed"
            }
            else -> {}
        }
    }

}

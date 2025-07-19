package com.example.exchangingprivatelessons.ui.profile

import androidx.lifecycle.*
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.common.util.toResultLiveData
import com.example.exchangingprivatelessons.domain.model.User
import com.example.exchangingprivatelessons.domain.usecase.user.*
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    observeUser: ObserveUser,
    private val updateProfile: UpdateProfile,
    private val deleteAccount: DeleteAccount
) : ViewModel() {

    /* ---------- data streams ---------- */

    /** זרם כ‑LiveData<Result<User>> עם Loading מופץ בתחילה */
    private val userResult: LiveData<Result<User>> =
        observeUser().toResultLiveData()

    /** UI‑state – null בזמן טעינה */
    val user: LiveData<User?> = userResult.map { res ->
        (res as? Result.Success<User>)?.data
    }

    /* ---------- one‑off snackbars ---------- */

    private val _snackbar = MutableLiveData<String>()
    val snackbar: LiveData<String> = _snackbar

    /* ---------- actions ---------- */

    fun save(display: String, bio: String, photo: String?) = viewModelScope.launch {
        when (val r = updateProfile(display.trim(), bio.trim(), photo?.trim())) {
            is Result.Failure -> _snackbar.postValue(r.throwable.localizedMessage ?: "Update failed")
            is Result.Success -> _snackbar.postValue("Profile updated")
            else              -> {}
        }
    }

    fun deleteMyAccount() = viewModelScope.launch {
        when (val r = deleteAccount()) {
            is Result.Failure -> _snackbar.postValue(r.throwable.localizedMessage ?: "Delete failed")
            is Result.Success -> _snackbar.postValue("Account deleted")
            else              -> {}
        }
    }
}

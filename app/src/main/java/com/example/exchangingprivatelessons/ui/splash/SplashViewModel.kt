/* SplashViewModel.kt */
package com.example.exchangingprivatelessons.ui.splash

import androidx.lifecycle.*
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.usecase.user.ObserveUser
import com.example.exchangingprivatelessons.domain.usecase.user.SignInOrUp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val auth       : FirebaseAuth,
    private val observeUser: ObserveUser,
    private val signInOrUp : SignInOrUp
) : ViewModel() {

    private val _authState = MutableLiveData(auth.currentUser != null)
    val   authState: LiveData<Boolean> = _authState

    private val _uiState = MutableLiveData(SplashUiState())
    val   uiState : LiveData<SplashUiState> = _uiState

    init {
        viewModelScope.launch {
            observeUser().collect { /* fire live‑sync */ }
        }
    }

    fun onLogin(email: String, pass: String) = viewModelScope.launch {
        _uiState.value = _uiState.value?.copy(loading = true, error = null)

        when (val res = signInOrUp(email, pass)) {
            is Result.Success -> {
                _authState.value = true
                _uiState.value   = SplashUiState(loggedIn = true)
            }
            is Result.Failure -> _uiState.value =
                SplashUiState(error = res.throwable.localizedMessage ?: "Unknown error")
            Result.Loading    -> {}
        }
    }
}

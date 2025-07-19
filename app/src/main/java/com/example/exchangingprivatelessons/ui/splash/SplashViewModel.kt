package com.example.exchangingprivatelessons.ui.splash

import androidx.lifecycle.*
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.usecase.user.SignInOrUp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val auth     : FirebaseAuth,
    private val signInOrUp: SignInOrUp          // ← use‑case injected
) : ViewModel() {

    /* auth state – האם כבר מחובר  */
    private val _authState = MutableLiveData(auth.currentUser != null)
    val authState : LiveData<Boolean> = _authState

    /* ui state */
    private val _uiState = MutableLiveData(SplashUiState())
    val uiState: LiveData<SplashUiState> = _uiState

    /** attempt login (נקרא ע״י מסך‑login אם צריך) */
    fun onLogin(email: String, pass: String) = viewModelScope.launch {
        _uiState.value = _uiState.value?.copy(loading = true, error = null)
        when (val res = signInOrUp(email, pass)) {
            is Result.Success -> {
                _authState.postValue(true)
                _uiState.postValue(SplashUiState(loggedIn = true))
            }
            is Result.Failure -> _uiState.postValue(
                SplashUiState(error = res.throwable.localizedMessage ?: "Unknown error")
            )
            else -> {}
        }
    }
}

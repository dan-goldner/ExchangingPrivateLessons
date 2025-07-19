package com.example.exchangingprivatelessons.ui.auth

import androidx.lifecycle.*
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.usecase.user.SignInOrUp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInOrUp: SignInOrUp
) : ViewModel() {

    enum class Mode { LOGIN, SIGNUP }

    data class UiState(
        val mode: Mode    = Mode.LOGIN,
        val loading: Boolean = false,
        val error: String?   = null
    )

    private val _uiState = MutableLiveData(UiState())
    val uiState: LiveData<UiState> = _uiState

    sealed interface UiEvent { object AuthSuccess : UiEvent }
    private val _event = MutableLiveData<UiEvent?>()
    val event: LiveData<UiEvent?> = _event

    fun toggleMode() {
        _uiState.value = _uiState.value?.let {
            it.copy(mode = if (it.mode == Mode.LOGIN) Mode.SIGNUP else Mode.LOGIN)
        }
    }

    fun authenticate(
        name: String,
        email: String,
        pass: String,
        confirm: String?,
        bio: String
    ) {
        val state = _uiState.value ?: return
        val isSignup = state.mode == Mode.SIGNUP

        /* ולידציה בסיסית */
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = state.copy(error = "Email address or password is empty")
            return
        }
        if (isSignup && pass != confirm) {
            _uiState.value = state.copy(error = "The passwords do not match")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(loading = true, error = null)

            val res = signInOrUp(
                email.trim(),
                pass,
                displayName = if (isSignup) name.trim() else null,
                bio         = if (isSignup) bio.trim()  else null
            )

            _uiState.value = _uiState.value?.copy(loading = false)

            when (res) {
                is Result.Success -> _event.value = UiEvent.AuthSuccess
                is Result.Failure -> _uiState.value =
                    _uiState.value?.copy(error = res.throwable.message ?: "Unknown error")
                Result.Loading    -> Unit
            }
        }
    }

    fun clearError() { _uiState.value = _uiState.value?.copy(error = null) }
    fun clearEvent() { _event.value = null }
}

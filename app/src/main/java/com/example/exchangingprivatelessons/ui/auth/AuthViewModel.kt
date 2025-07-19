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

    /* ---------- UI state ---------- */
    enum class Mode { LOGIN, SIGNUP }

    data class UiState(
        val mode: Mode = Mode.LOGIN,
        val loading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableLiveData(UiState())
    val uiState: LiveData<UiState> = _uiState

    /* ---------- One-time events (navigation, Toast…) ---------- */
    sealed interface UiEvent {
        object AuthSuccess : UiEvent
    }

    private val _event = MutableLiveData<UiEvent?>()
    val event: LiveData<UiEvent?> = _event

    fun toggleMode() {
        _uiState.value = _uiState.value?.copy(
            mode = if (_uiState.value?.mode == Mode.LOGIN) Mode.SIGNUP else Mode.LOGIN
        )
    }

    fun authenticate(name: String, email: String, pass: String, confirm: String?) {
        val state = _uiState.value ?: return
        val mode = state.mode

        // לקוח ולידציה בסיסית
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = state.copy(error = "Email address or password is empty")
            return
        }
        if (mode == Mode.SIGNUP && pass != confirm) {
            _uiState.value = state.copy(error = "The passwords do not match")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(loading = true, error = null)

            val result = signInOrUp(
                email.trim(),
                pass,
                displayName = if (mode == Mode.SIGNUP) name.trim() else null
            )

            _uiState.value = _uiState.value?.copy(loading = false)

            when (result) {
                is Result.Success -> _event.value = UiEvent.AuthSuccess
                is Result.Failure -> _uiState.value = _uiState.value?.copy(
                    error = result.throwable.message ?: "Unknown error"
                )
                is Result.Loading -> {} // אין צורך כאן כי אנחנו מנהלים loading עם השדה
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value?.copy(error = null)
    }

    fun clearEvent() {
        _event.value = null
    }
}

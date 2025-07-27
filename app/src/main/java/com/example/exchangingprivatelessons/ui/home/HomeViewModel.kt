// app/src/main/java/com/example/exchangingprivatelessons/ui/home/HomeViewModel.kt
package com.example.exchangingprivatelessons.ui.home

import android.util.Log
import androidx.lifecycle.*
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.usecase.lesson.*
import com.example.exchangingprivatelessons.domain.usecase.request.*
import com.example.exchangingprivatelessons.domain.usecase.user.ObserveUser
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map          // ⬅️ הוסף import
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeLessons         : ObserveLessons,
    private val observeIncomingRequests: ObserveIncomingRequests,
    private val refreshLessons         : RefreshLessons,
    private val refreshLessonRequests  : RefreshLessonRequests,
    observeUser                        : ObserveUser
) : ViewModel() {

    /* ---------- UI state ---------- */
    private val _state = MutableLiveData<HomeUiState>(HomeUiState.Loading)
    val state: LiveData<HomeUiState> = _state

    val userName: LiveData<String> =
        observeUser()                 // Flow<Result<User>>
            .map { res ->
                when (res) {
                    is Result.Success -> {
                        val u = res.data
                        if (u.displayName.isNotBlank()) u.displayName
                        else u.email.substringBefore('@')        // fallback
                    }
                    else -> ""                                   // Loading / Failure
                }
            }
            .asLiveData()

    val userScore: LiveData<Int> =
        observeUser()                      // Flow<Result<User>>
            .map { res ->
                when (res) {
                    is Result.Success -> res.data.score      // ← מפה ל‑Int
                    else              -> 0                   // Loading / Failure
                }
            }
            .asLiveData()


    /* ---------- init ---------- */
    init {
        observeHomeFeed()   // Listener קבוע
        refresh()           // משיכה ראשונית
    }

    /* ---------- Feed ---------- */
    private fun observeHomeFeed() = viewModelScope.launch {
        combine(
            observeLessons(),           // Flow<Result<List<ViewLesson>>>
            observeIncomingRequests()   // Flow<Result<List<LessonRequest>>>
        ) { lessonsRes, requestsRes ->
            when {
                lessonsRes is Result.Loading || requestsRes is Result.Loading ->
                    HomeUiState.Loading

                lessonsRes is Result.Failure ->
                    HomeUiState.Error(lessonsRes.throwable.localizedMessage ?: "Unexpected error")

                requestsRes is Result.Failure ->
                    HomeUiState.Error(requestsRes.throwable.localizedMessage ?: "Unexpected error")

                lessonsRes is Result.Success && requestsRes is Result.Success ->
                    HomeUiState.Content(
                        lessons  = lessonsRes.data,
                        incoming = requestsRes.data
                    )

                else -> HomeUiState.Error("Unknown state")
            }
        }.collect { _state.postValue(it) }
    }

    /** Pull‑to‑refresh או יזום מה‑init */
    fun refresh() = viewModelScope.launch {
        refreshLessons()
        refreshLessonRequests()
        Log.d("Auth", "current uid = ${FirebaseAuth.getInstance().currentUser?.uid}")

    }
}

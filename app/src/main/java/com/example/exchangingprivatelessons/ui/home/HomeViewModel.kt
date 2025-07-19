// app/src/main/java/com/example/exchangingprivatelessons/ui/home/HomeViewModel.kt
package com.example.exchangingprivatelessons.ui.home

import androidx.lifecycle.*
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.usecase.lesson.ObserveLessons
import com.example.exchangingprivatelessons.domain.usecase.request.ObserveIncomingRequests
import com.example.exchangingprivatelessons.domain.usecase.lesson.RefreshLessons
import com.example.exchangingprivatelessons.domain.usecase.request.RefreshLessonRequests
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeLessons: ObserveLessons,
    private val observeIncomingRequests: ObserveIncomingRequests,
    private val refreshLessons: RefreshLessons,
    private val refreshLessonRequests: RefreshLessonRequests
) : ViewModel() {

    private val _state = MutableLiveData<HomeUiState>(HomeUiState.Loading)
    val state: LiveData<HomeUiState> = _state

    init { observeHomeFeed() }

    private fun observeHomeFeed() = viewModelScope.launch {
        combine(
            observeLessons(),           // Flow<Result<List<Lesson>>>
            observeIncomingRequests()   // Flow<Result<List<LessonRequest>>>
        ) { lessonsRes, requestsRes ->
            when {
                lessonsRes is Result.Loading || requestsRes is Result.Loading ->
                    HomeUiState.Loading

                lessonsRes is Result.Failure ->
                    HomeUiState.Error(lessonsRes.throwable.localizedMessage ?: "Unexpected error")

                requestsRes is Result.Failure ->
                    HomeUiState.Error(requestsRes.throwable.localizedMessage ?: "Unexpected error")


                lessonsRes is Result.Success && requestsRes is Result.Success ->HomeUiState.Content(
                    lessons  = lessonsRes.data,    //  List<ViewLesson>
                    incoming = requestsRes.data
                )


                else -> HomeUiState.Error("Unknown state")
            }
        }.collect { _state.postValue(it) }
    }

    /** Pull‑to‑refresh או לחיצה על כפתור רענון */
    fun refresh() = viewModelScope.launch {
        refreshLessons()
        refreshLessonRequests()
    }
}

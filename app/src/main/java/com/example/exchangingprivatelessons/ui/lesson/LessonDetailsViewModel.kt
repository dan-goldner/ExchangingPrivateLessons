package com.example.exchangingprivatelessons.ui.lesson

import androidx.lifecycle.*
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.model.ViewLesson
import com.example.exchangingprivatelessons.domain.usecase.lesson.GetLessonDetails
import com.example.exchangingprivatelessons.domain.usecase.request.RequestLesson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonDetailsViewModel @Inject constructor(
    private val getLessonDetailsUseCase : GetLessonDetails,
    private val requestLessonUseCase    : RequestLesson
) : ViewModel() {

    private val _state = MutableLiveData(DetailsState(loading = true))
    val state: LiveData<DetailsState> = _state

    private var currentLessonId: String? = null

    /* ---------- public API ---------- */

    fun loadLesson(id: String) {
        currentLessonId = id
        viewModelScope.launch {
            _state.value = DetailsState(loading = true)

            when (val res = getLessonDetailsUseCase(id)) {
                is Result.Success -> _state.value = DetailsState(lesson = res.data)
                is Result.Failure -> _state.value = DetailsState(errorMsg = res.throwable.message)
                is Result.Loading -> _state.value = DetailsState(loading = true)
            }
        }
    }

    fun onRequestLesson() {
        val lesson = state.value?.lesson ?: return
        viewModelScope.launch {
            requestLessonUseCase.requestLesson(
                lessonId = lesson.id,
                ownerId = lesson.ownerId
            )
        }
    }


    fun refresh() = currentLessonId?.let(::loadLesson)

    /* ---------- UI‑state holder ---------- */
    data class DetailsState(
        val lesson  : ViewLesson? = null,
        val loading : Boolean = false,
        val errorMsg: String? = null
    ) {
        val canEdit    get() = lesson?.canEdit    == true
        val canRequest get() = lesson?.canRequest == true
        val canRate    get() = lesson?.canRate    == true
        /** נוח ל‑UI */
        val ownerPhotoUrl get() = lesson?.ownerPhotoUrl
    }

}

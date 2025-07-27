package com.example.exchangingprivatelessons.ui.lesson

import androidx.lifecycle.*
import com.example.exchangingprivatelessons.common.di.UseCaseModule.requestLesson
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.model.RequestStatus
import com.example.exchangingprivatelessons.domain.model.ViewLesson
import com.example.exchangingprivatelessons.domain.usecase.lesson.GetLessonDetails
import com.example.exchangingprivatelessons.domain.usecase.request.RequestLesson
import com.example.exchangingprivatelessons.domain.usecase.request.RefreshLessonRequests
import com.google.android.material.snackbar.Snackbar
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonDetailsViewModel @Inject constructor(
    private val getLessonDetails : GetLessonDetails,
    private val requestLessonUse : RequestLesson,
    private val refreshRequests  : RefreshLessonRequests          // ←  NEW
) : ViewModel() {

    /* ---------- Screen‑state ---------- */

    private val _state    = MutableLiveData(DetailsState(loading = true))
    val   state : LiveData<DetailsState> = _state

    private val _snackbar = MutableLiveData<String?>()            // ←  NEW
    val   snackbar : LiveData<String?> = _snackbar

    private var currentLessonId: String? = null

    /* ---------- load / refresh ---------- */

    fun loadLesson(id: String) {
        currentLessonId = id
        viewModelScope.launch { fetchLesson(id) }
    }

    fun refresh() = currentLessonId?.let { loadLesson(it) }

    private suspend fun fetchLesson(id: String) {
        _state.postValue(DetailsState(loading = true))
        when (val res = getLessonDetails(id)) {
            is Result.Success -> _state.postValue(DetailsState(lesson = res.data))
            is Result.Failure -> _state.postValue(DetailsState(errorMsg = res.throwable?.message))
            Result.Loading    -> _state.postValue(DetailsState(loading = true))
        }
    }

    /* ---------- Request lesson ---------- */

    fun onRequestLesson() {
        val lesson = _state.value?.lesson ?: return

        viewModelScope.launch {
            // ❶ ה‑Use‑case נקרא כ‑invoke
            when (val res = requestLessonUse(lesson.id, lesson.ownerId)) {

                /* ☑︎ הצלחה */
                is Result.Success<*> -> {                 // ❸ star‑projection
                    _snackbar.postValue("✔ Request sent")
                    refreshRequests()
                    refresh()
                }

                /* ❌ כשל */
                is Result.Failure -> {
                    val msg = when (res.throwable.message ?: "") {
                        "LOW_SCORE_LOCAL", "LOW_SCORE"  -> "You have –3 points — to request a new lesson, you must first give one."
                        "already-exists"                -> "There is already an open request for this lesson."
                        else                            -> "The action failed — please try again."
                    }
                    _snackbar.postValue(msg)
                }


                Result.Loading -> TODO()
            }
        }
    }


    fun snackbarShown() { _snackbar.value = null }

    /* ---------- DTO למצב‑המסך ---------- */
    data class DetailsState(
        val lesson  : ViewLesson? = null,
        val loading : Boolean     = false,
        val errorMsg: String?     = null
    ) {
        /* הרשאות / סטטוסים מחושבים */
        val canEdit     get() = lesson?.canEdit    == true
        val canRate     get() = lesson?.canRate    == true
        val canRequest  get() = lesson?.canRequest == true
        val pending     get() = lesson?.myRequestStatus == RequestStatus.Pending
    }
}

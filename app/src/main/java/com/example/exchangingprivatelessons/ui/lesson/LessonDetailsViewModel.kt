package com.example.exchangingprivatelessons.ui.lesson

import androidx.lifecycle.*
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.model.RequestStatus
import com.example.exchangingprivatelessons.domain.model.ViewLesson
import com.example.exchangingprivatelessons.domain.usecase.lesson.GetLessonDetails
import com.example.exchangingprivatelessons.domain.usecase.request.RequestLesson
import com.example.exchangingprivatelessons.domain.usecase.request.RefreshLessonRequests
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
            when (val r = requestLessonUse.requestLesson(lesson.id, lesson.ownerId)) {
                is Result.Success -> {
                    _snackbar.postValue("בקשה נשלחה")
                    refreshRequests()                 // מסנכרן לטבלה המקומית  (Room)
                    refresh()                         // מביא את ה‑pending ל‑UI
                }
                is Result.Failure -> _snackbar.postValue(
                    r.throwable?.localizedMessage ?: "הבקשה נכשלה")
                else -> {}
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

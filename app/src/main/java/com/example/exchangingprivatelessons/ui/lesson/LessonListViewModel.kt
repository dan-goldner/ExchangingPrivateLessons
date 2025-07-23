package com.example.exchangingprivatelessons.ui.lesson

import android.content.Context
import androidx.lifecycle.*
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.common.util.toResultLiveData
import com.example.exchangingprivatelessons.domain.model.ViewLesson
import com.example.exchangingprivatelessons.domain.usecase.lesson.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonListViewModel @Inject constructor(
    private val observeLessons : ObserveLessons,
    private val observeTaken   : ObserveTakenLessons,
    private val refreshLessons : RefreshLessons,
    private val archiveLesson  : ArchiveLesson,
    @ApplicationContext private val ctx: Context
) : ViewModel() {

    /* ───────────── מצב רשימה ───────────── */
    enum class Mode { AVAILABLE, MINE, TAKEN }

    private val mode = MutableLiveData(Mode.AVAILABLE)

    /**  זרם תוצאות (Flow) → LiveData<Result<…>>  */
    private val lessonsRes: LiveData<Result<List<ViewLesson>>> =
        mode.switchMap { m ->
            when (m) {
                Mode.AVAILABLE -> observeLessons()
                Mode.MINE      -> observeLessons(true)
                Mode.TAKEN     -> observeTaken()
            }.toResultLiveData()
        }

    /*  אינדיקציה ל‑Swipe‑to‑Refresh */
    private val _refreshing = MutableLiveData(false)

    /* ───────────── UI‑State מאוחד ───────────── */
    private val _uiState = MediatorLiveData<UiState>().apply {
        fun build(): UiState {
            val res = lessonsRes.value
            return when (res) {
                null, is Result.Loading -> UiState(
                    loading    = true,
                    refreshing = _refreshing.value ?: false
                )
                is Result.Failure -> UiState(
                    errorMsg   = res.throwable?.localizedMessage,
                    refreshing = _refreshing.value ?: false
                )
                is Result.Success -> UiState(
                    lessons    = res.data.map { it.toItem(ctx) },
                    refreshing = _refreshing.value ?: false
                )
            }
        }
        addSource(lessonsRes)  { value = build() }
        addSource(_refreshing) { value = build() }
    }
    val uiState: LiveData<UiState> = _uiState   // חשיפה לקריאה בלבד

    /* ───────────── API לצריכת ה‑UI ───────────── */

    fun setMode(m: Mode) { if (mode.value != m) mode.value = m }

    fun onRefresh() = viewModelScope.launch {
        _refreshing.value = true
        refreshLessons()
        _refreshing.value = false
    }

    fun onArchiveToggle(id: String, archived: Boolean) =
        viewModelScope.launch { archiveLesson(id, archived) }

    /** ה‑UI קרא את השגיאה – מאפסים אותה */
    fun errorShown() {
        _uiState.value = _uiState.value?.copy(errorMsg = null)
    }

    /* ───────────── מייצג מצב מסך ───────────── */
    data class UiState(
        val lessons   : List<LessonItem> = emptyList(),
        val loading   : Boolean          = false,
        val refreshing: Boolean          = false,
        val errorMsg  : String?          = null
    )
}

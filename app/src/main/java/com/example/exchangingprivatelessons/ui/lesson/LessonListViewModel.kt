package com.example.exchangingprivatelessons.ui.lesson

import android.content.Context
import androidx.lifecycle.*
import com.example.exchangingprivatelessons.common.di.UseCaseModule.observeLessons
import com.example.exchangingprivatelessons.common.di.UseCaseModule_ObserveLessonsFactory.observeLessons
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

    /* snack‑bar חד‑פעמי */
    private val _snackbar = MutableLiveData<String?>()
    val snackbar: LiveData<String?> = _snackbar
    /**  זרם תוצאות (Flow) → LiveData<Result<…>>  */
    /* LessonListViewModel.kt */
    private val lessonsRes: LiveData<Result<List<ViewLesson>>> =
        mode.switchMap { m ->
            when (m) {
                Mode.AVAILABLE -> observeLessons()                    // רק Active
                Mode.MINE      -> observeLessons(
                    onlyMine = true,
                )
                Mode.TAKEN     -> observeTaken()                      // רק Active
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
                    lessons    = res.data,        // ← 그대로
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

    fun onArchiveToggle(id: String, archived: Boolean) = viewModelScope.launch {
            _snackbar.value = if (archived) "הועבר לארכיון" else "הוחזר מהרשימה"
            when (archiveLesson(id, archived)) {
                    is Result.Failure -> _snackbar.value = "הפעולה נכשלה – נסה שוב"
                    else              -> {}   // ה‑UI יתעדכן אוטומטית מה‑Flow
            }
    }

    fun snackbarShown() { _snackbar.value = null }

    /** ה‑UI קרא את השגיאה – מאפסים אותה */
    fun errorShown() {
        _uiState.value = _uiState.value?.copy(errorMsg = null)
    }

    /* ───────────── מייצג מצב מסך ───────────── */
    /* Ui‑state */
    data class UiState(
        val lessons   : List<ViewLesson> = emptyList(),   // ← ViewLesson
        val loading   : Boolean          = false,
        val refreshing: Boolean          = false,
        val errorMsg  : String?          = null
    )

}

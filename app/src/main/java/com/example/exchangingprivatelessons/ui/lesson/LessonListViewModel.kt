package com.example.exchangingprivatelessons.ui.lesson

import android.content.Context
import androidx.lifecycle.*
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.usecase.lesson.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import com.example.exchangingprivatelessons.ui.lesson.toItem

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

    enum class Mode { AVAILABLE, MINE, TAKEN }

    private val _ui = MutableLiveData(UiState(loading = true))
    val uiState: LiveData<UiState> = _ui

    private var job: Job? = null
    private var currentMode = Mode.AVAILABLE

    init { collect(Mode.AVAILABLE) }

    fun setMode(m: Mode) { if (m != currentMode) collect(m) }

    fun onRefresh() = launch {
        _ui.postValue(_ui.value?.copy(refreshing = true))
        refreshLessons()
        _ui.postValue(_ui.value?.copy(refreshing = false))
    }

    fun onArchiveToggle(id: String, archived: Boolean) = launch {
        archiveLesson(id, archived)
    }

    fun errorShown() { _ui.postValue(_ui.value?.copy(errorMsg = null)) }

    /* ---------- private ---------- */

    private fun collect(mode: Mode) {
        job?.cancel()
        currentMode = mode

        job = launch {
            _ui.postValue(_ui.value?.copy(loading = true))

            val flow = when (mode) {
                Mode.AVAILABLE -> observeLessons()        // ‎כבר מחזיר ‎ViewLesson
                Mode.MINE      -> observeLessons(true)    // ‎ idem
                Mode.TAKEN     -> observeTaken()          // ‎ idem
            }

            flow.collect { res ->
                when (res) {
                    is Result.Loading  -> _ui.postValue(_ui.value?.copy(loading = true))
                    is Result.Failure  -> _ui.postValue(
                        _ui.value?.copy(loading = false,
                            errorMsg = res.throwable?.localizedMessage)
                    )
                    is Result.Success -> _ui.postValue(
                        UiState(
                            lessons = res.data.map { it.toItem(ctx) },
                            loading = false
                        )
                    )
                }
            }
        }
    }

    private fun launch(block: suspend () -> Unit) =
        viewModelScope.launch { block() }

    /* ---------- UI state ---------- */

    data class UiState(
        val lessons   : List<LessonItem> = emptyList(),
        val loading   : Boolean          = false,
        val refreshing: Boolean          = false,
        val errorMsg  : String?          = null
    )
}

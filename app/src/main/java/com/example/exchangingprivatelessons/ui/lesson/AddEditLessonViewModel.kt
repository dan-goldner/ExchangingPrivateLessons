package com.example.exchangingprivatelessons.ui.lesson

import android.net.Uri
import androidx.lifecycle.*
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.repository.LessonRepository
import com.example.exchangingprivatelessons.domain.usecase.lesson.CreateLesson
import com.example.exchangingprivatelessons.domain.usecase.lesson.UpdateLesson
import com.example.exchangingprivatelessons.domain.usecase.lesson.RefreshLessons
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditLessonViewModel @Inject constructor(
    private val repo: LessonRepository,
    private val createLesson: CreateLesson,
    private val updateLesson: UpdateLesson,
    private val refreshLessons: RefreshLessons,
    savedStateHandle: SavedStateHandle          // lessonId מגיע מפאראם ה‑Nav
) : ViewModel() {

    private val lessonId: String? = savedStateHandle.get<String>("lessonId")

    private val _ui = MutableLiveData(AddEditUiState())
    val ui: LiveData<AddEditUiState> = _ui            // חשוף לקריאה בלבד

    init {
        if (!lessonId.isNullOrBlank()) {
            loadExisting(lessonId)
        }
    }

    /* ---------- public events ---------- */

    fun onImageChosen(uri: Uri) = updateUi { copy(imageUri = uri) }

    fun onSaveClicked(title: String, description: String) = viewModelScope.launch {
        updateUi { copy(
            loading = true,
            errorMsg = null,
            savedLessonId = null,
            existingLesson = null
        ) }

        val savedId: String? = if (lessonId.isNullOrBlank()) {
            val res = createLesson(title, description, _ui.value?.imageUri?.toString())
            if (res is Result.Success) {
                refreshLessons() // ✅ Refresh after creating lesson
                res.data
            } else return@launch updateUi {
                copy(
                    loading = false,
                    errorMsg = (res as? Result.Failure)?.throwable?.localizedMessage
                )
            }
        } else {
            val res = updateLesson(
                lessonId,
                title,
                description,
                _ui.value?.imageUri?.toString()
            )
            if (res is Result.Failure) return@launch updateUi {
                copy(loading = false, errorMsg = res.throwable.localizedMessage)
            }
            lessonId
        }

        updateUi { copy(loading = false, savedLessonId = savedId) }


    }

    /* ---------- private ---------- */

    private fun loadExisting(id: String) = viewModelScope.launch {
        repo.observeLesson(id).asLiveData().observeForever { res ->
            if (res is Result.Success) {
                updateUi { copy(existingLesson = res.data) }
            }
        }
    }

    /** helper לעדכון אטומי של ה‑State */
    private inline fun updateUi(block: AddEditUiState.() -> AddEditUiState) {
        _ui.postValue(_ui.value?.block())
    }
}

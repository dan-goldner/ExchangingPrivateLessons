// app/src/main/java/com/example/exchangingprivatelessons/ui/home/HomeUiState.kt
package com.example.exchangingprivatelessons.ui.home

import com.example.exchangingprivatelessons.domain.model.Lesson
import com.example.exchangingprivatelessons.domain.model.LessonRequest
import com.example.exchangingprivatelessons.domain.model.ViewLesson

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Content(
        val lessons: List<ViewLesson>,
        val incoming: List<LessonRequest>
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState

}

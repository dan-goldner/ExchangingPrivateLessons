package com.example.exchangingprivatelessons.ui.lesson

import android.net.Uri
import com.example.exchangingprivatelessons.domain.model.Lesson

data class AddEditUiState(
    val loading: Boolean          = false,
    val imageUri: Uri?            = null,
    val existingLesson: Lesson?   = null,
    val savedLessonId: String?    = null,
    val errorMsg: String?         = null,
    val justDeleted: Boolean = false
)

package com.example.exchangingprivatelessons.domain.repository

import com.example.exchangingprivatelessons.domain.model.TakenLesson
import kotlinx.coroutines.flow.Flow
import com.example.exchangingprivatelessons.common.util.Result

interface TakenLessonRepository {

    /** Stream lessons that the current user is approved to take. */
    fun observeTakenLessons(): Flow<Result<List<TakenLesson>>>
}

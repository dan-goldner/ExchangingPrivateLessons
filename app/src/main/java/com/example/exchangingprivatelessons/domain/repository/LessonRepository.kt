package com.example.exchangingprivatelessons.domain.repository

import com.example.exchangingprivatelessons.domain.model.Lesson
import kotlinx.coroutines.flow.Flow
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.model.LessonRequest
import com.example.exchangingprivatelessons.domain.usecase.request.RequestLesson

interface LessonRepository {

    /** feed ראשי – ניתן לסנן לשיעורים שלי בלבד. */
    fun observeLessons(onlyMine: Boolean = false): Flow<Result<List<Lesson>>>

    fun observeLesson(lessonId: String): Flow<Result<Lesson>>

    fun observeTakenLessons(userId: String): Flow<Result<List<Lesson>>>

    suspend fun getApprovedLessonRequestsForUser(userId: String): List<LessonRequest>

    suspend fun refreshMineLessons(userId: String)

    suspend fun deleteLesson(lessonId: String): Result<Unit>

    /* refresh + mutations */
    suspend fun forceRefreshLessons(): Result<Unit>
    suspend fun updateLesson(
        lessonId: String,
        title: String? = null,
        description: String? = null,
    ): Result<Unit>



    suspend fun getLesson(lessonId: String): Result<Lesson>

    suspend fun archiveLesson(lessonId: String, archived: Boolean): Result<Unit>

    suspend fun createLesson(
        title: String,
        description: String,
    ): Result<String>
}

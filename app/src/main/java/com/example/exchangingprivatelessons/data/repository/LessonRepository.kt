package com.example.exchangingprivatelessons.data.repository

import com.example.exchangingprivatelessons.data.dao.LessonDao
import com.example.exchangingprivatelessons.data.entity.LessonEntity
import com.example.exchangingprivatelessons.data.entity.toEntity
import com.example.exchangingprivatelessons.data.entity.toModel
import com.example.exchangingprivatelessons.model.Lesson

/* – Firebase – */
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await      // await() על Task

/* – Coroutines / Flow – */
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LessonRepository(
    private val dao: LessonDao,
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val io: CoroutineDispatcher = Dispatchers.IO
) {

    /** זרם הנתונים מתוך Room – מומר ל-Model */
    val lessons: Flow<List<Lesson>> =
        dao.getAll().map { list -> list.map { it.toModel() } }

    /** סנכרון חד־פעמי Firestore → Room */
    suspend fun syncFromCloud() = withContext(io) {
        val local = firestore.collection("lessons").get().await()
            .documents
            .map { doc ->
                LessonEntity(
                    id          = doc.id,
                    title       = doc.getString("title").orEmpty(),
                    description = doc.getString("description").orEmpty()
                )
            }
        dao.insertAll(local)
    }

    /** הוספת שיעור חדש לענן + Room */
    suspend fun addLesson(lesson: Lesson) = withContext(io) {
        val docRef = firestore.collection("lessons")
            .add(mapOf("title" to lesson.title,
                "description" to lesson.description))
            .await()

        dao.insert(lesson.copy(id = docRef.id).toEntity())
    }
}

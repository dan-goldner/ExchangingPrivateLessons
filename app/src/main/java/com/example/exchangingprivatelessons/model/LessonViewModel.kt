package com.example.exchangingprivatelessons.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.exchangingprivatelessons.model.Lesson
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LessonViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val _lessons = MutableLiveData<List<Lesson>>(emptyList())
    val lessons: LiveData<List<Lesson>> = _lessons

    init {
        fetchLessons()
    }

    fun fetchLessons() {
        db.collection("lessons")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.map { doc ->
                    doc.toObject(Lesson::class.java)!!.copy(id = doc.id)
                }
                _lessons.value = list
            }
            .addOnFailureListener {
                Log.e("LessonVM", "fetch failed", it)
            }
    }

    fun addLesson(lesson: Lesson) {
        db.collection("lessons")
            .add( mapOf(
                "title" to lesson.title,
                "description" to lesson.description
            ))
            .addOnSuccessListener {
                fetchLessons()
            }
            .addOnFailureListener {
                Log.e("LessonVM", "add failed", it)
            }
    }
}

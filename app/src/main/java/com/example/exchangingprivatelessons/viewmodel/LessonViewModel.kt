package com.example.exchangingprivatelessons.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exchangingprivatelessons.data.db.AppDatabase
import com.example.exchangingprivatelessons.data.repository.LessonRepository
import com.example.exchangingprivatelessons.model.Lesson
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import androidx.lifecycle.asLiveData


class LessonViewModel(app: Application) : AndroidViewModel(app) {

    private val repo: LessonRepository = LessonRepository(
        AppDatabase.get(app).lessonDao()
    )

    // LiveData שנובע מ-Room
    val lessons = repo.lessons.asLiveData()

    init {
        viewModelScope.launch { repo.syncFromCloud() }
    }

    fun addLesson(l: Lesson) = viewModelScope.launch {
        repo.addLesson(l)
    }
}

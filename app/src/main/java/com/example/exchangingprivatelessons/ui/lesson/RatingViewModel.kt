package com.example.exchangingprivatelessons.ui.lesson

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.usecase.rating.RateLesson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RatingViewModel @Inject constructor(
    private val rateLesson: RateLesson
) : ViewModel() {

    private lateinit var lessonId: String

    private val _rating  = MutableLiveData(0)
    private val _comment = MutableLiveData("")

    val rating : LiveData<Int>    = _rating
    val comment: LiveData<String> = _comment

    fun init(id: String) { lessonId = id }          // נקרא מה‑Fragment
    fun setRating(v: Int)  { _rating.value  = v }
    fun setComment(v: String) { _comment.value = v }

    fun save(onComplete: (Boolean) -> Unit) = viewModelScope.launch {
        val res = rateLesson(lessonId, rating.value ?: 0, comment.value)
        onComplete(res is Result.Success)
    }
}

package com.example.exchangingprivatelessons.domain.usecase.request



import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.repository.LessonRequestRepository
import javax.inject.Inject
import javax.inject.Singleton
@Singleton class CancelRequest @Inject constructor(
    private val repo: LessonRequestRepository
){
    suspend operator fun invoke(id: String) = repo.cancelRequest(id)
}
package com.example.exchangingprivatelessons.ui.request

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.domain.model.LessonRequest
import com.example.exchangingprivatelessons.domain.model.RequestStatus
import com.example.exchangingprivatelessons.domain.usecase.request.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RequestsViewModel @Inject constructor(
    private val observeByStatus: ObserveRequestsByStatus,
    private val approveRequest : ApproveRequest,
    private val declineRequest : DeclineRequest,
    private val cancelRequest  : CancelRequest
) : ViewModel() {

    private val _status = MutableLiveData(RequestStatus.Pending)
    val status: LiveData<RequestStatus> get() = _status

    private val _requests = MutableLiveData<List<LessonRequest>>(emptyList())
    val requests: LiveData<List<LessonRequest>> get() = _requests

    private val _snackbar = MutableLiveData<String>()
    val snackbar: LiveData<String> get() = _snackbar

    init {
        observeRequests()
    }

    fun setStatus(status: RequestStatus) {
        _status.value = status
        observeRequests()
    }

    private fun observeRequests() {
        val currentStatus = _status.value ?: return
        viewModelScope.launch {
            observeByStatus(currentStatus).collect { res ->
                val list = (res as? Result.Success)?.data ?: emptyList()
                _requests.postValue(list)
            }
        }
    }

    fun approve(id: String) = doAction { approveRequest(id) }
    fun decline(id: String) = doAction { declineRequest(id) }
    fun cancel(id: String)  = doAction { cancelRequest(id)  }

    private fun doAction(block: suspend () -> Result<Unit>) = viewModelScope.launch {
        when (val r = block()) {
            is Result.Failure -> _snackbar.postValue(
                r.throwable.localizedMessage ?: "Operation failed"
            )

            is Result.Success -> _snackbar.postValue("Done")
            else -> {}
        }
    }
}
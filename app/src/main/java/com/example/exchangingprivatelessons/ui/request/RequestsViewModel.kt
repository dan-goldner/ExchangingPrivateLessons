package com.example.exchangingprivatelessons.ui.request

import androidx.lifecycle.*
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.common.util.toResultLiveData
import com.example.exchangingprivatelessons.domain.model.LessonRequest
import com.example.exchangingprivatelessons.domain.model.RequestStatus
import com.example.exchangingprivatelessons.domain.usecase.request.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RequestsViewModel @Inject constructor(
    private val observeIncoming   : ObserveIncomingRequests,
    private val observeByStatus   : ObserveRequestsByStatus,
    private val approveRequestUse : ApproveRequest,
    private val declineRequestUse : DeclineRequest,
    private val cancelRequestUse  : CancelRequest
) : ViewModel() {

    /* ───────────── UI‑state keys ───────────── */
    enum class Mode { SENT , RECEIVED }

    private val _mode   = MutableLiveData(Mode.RECEIVED)
    private val _status = MutableLiveData(RequestStatus.Pending)   // רלוונטי רק למצב SENT
    private val _snack  = MutableLiveData<String>()

    /** טריגר מאוחד → Pair<Mode,Status> */
    private val trigger = MediatorLiveData<Pair<Mode,RequestStatus>>().apply {
        value = _mode.value!! to _status.value!!                 // ערך התחלתי
        addSource(_mode)   { value = it to (_status.value ?: RequestStatus.Pending) }
        addSource(_status) { value = (_mode.value ?: Mode.RECEIVED) to it }
    }

    /* ───────────── נתונים מה‑Use‑cases ───────────── */

    /** Result<List<LessonRequest>> ע״פ mode+status  */
    private val _requestsResult: LiveData<Result<List<LessonRequest>>> =
        trigger.switchMap { (mode,status) ->
            when (mode) {
                Mode.RECEIVED -> observeIncoming()                 // Flow<Result<…>>
                    .toResultLiveData()                            // ↙︎
                Mode.SENT     -> observeByStatus(status)           // Flow<Result<…>>
                    .toResultLiveData()
            }
        }

    /** רשימת הבקשות לממשק‑המשתמש */
    val requests: LiveData<List<LessonRequest>> =
        _requestsResult.map { res ->
            (res as? Result.Success)?.data ?: emptyList()
        }

    val mode    : LiveData<Mode>            = _mode
    val status  : LiveData<RequestStatus>   = _status
    val snackbar: LiveData<String>          = _snack

    /* ───────────── מיתוג מצב / סטטוס ───────────── */

    fun setMode(m: Mode) {
        if (_mode.value != m) _mode.value = m
    }

    fun setStatus(s: RequestStatus) {
        if (_status.value != s) _status.value = s
    }

    /* ───────────── פעולות על בקשה ───────────── */

    fun approve(id: String) = doAction { approveRequestUse(id) }
    fun decline(id: String) = doAction { declineRequestUse(id) }
    fun cancel (id: String) = doAction { cancelRequestUse(id)  }

    private fun doAction(block: suspend () -> Result<Unit>) = viewModelScope.launch {
        when (val r = block()) {
            is Result.Success -> _snack.postValue("Done")
            is Result.Failure -> _snack.postValue(
                r.throwable.localizedMessage ?: "Operation failed")
            else -> {}
        }
    }
}

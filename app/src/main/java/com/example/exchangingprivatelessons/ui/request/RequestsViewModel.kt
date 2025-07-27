package com.example.exchangingprivatelessons.ui.request

import androidx.lifecycle.*
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.common.util.toResultLiveData
import com.example.exchangingprivatelessons.domain.model.LessonRequest
import com.example.exchangingprivatelessons.domain.model.RequestStatus
import com.example.exchangingprivatelessons.domain.usecase.request.*
import androidx.lifecycle.viewModelScope
import com.example.exchangingprivatelessons.domain.model.ViewRequestItem
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RequestsViewModel @Inject constructor(
    private val buildViewReqs : BuildViewRequests,
    private val approveReq    : ApproveRequest,
    private val declineReq    : DeclineRequest,
    private val cancelReq     : CancelRequest
) : ViewModel() {

    enum class Mode { SENT , RECEIVED }

    private val _mode   = MutableLiveData(Mode.RECEIVED)



    private val _snack  = MutableLiveData<String>()

    private val _status = MutableLiveData<RequestStatus?>(null)

    private val trigger = MediatorLiveData<Pair<Mode, RequestStatus?>>().apply {
        fun post(m: Mode, s: RequestStatus?) { value = m to s }
        post(Mode.RECEIVED, null)
        addSource(_mode)   { post(it, _status.value) }
        addSource(_status) { post(_mode.value ?: Mode.RECEIVED, it) }
    }





    /** ‎UI‑items  עם כל הפרטים */
    val items: LiveData<List<ViewRequestItem>> =
        trigger.switchMap { (m,s) ->
            buildViewReqs(m, s).toResultLiveData()
        }.map { (it as? Result.Success)?.data ?: emptyList() }

    val snackbar: LiveData<String> = _snack
    fun setMode(m: Mode) {
        _mode.value = m
        if (m == Mode.RECEIVED) _status.value = RequestStatus.Pending // לא רלוונטי
    }

    fun setStatus(s: RequestStatus) { if (_mode.value == Mode.SENT) _status.value = s }


    fun approve(id: String) = doAction { approveReq(id) }
    fun decline(id: String) = doAction { declineReq(id) }
    fun cancel (id: String) = doAction { cancelReq (id) }


    private fun doAction(block: suspend () -> Result<Unit>) =
        viewModelScope.launch {
            when (val r = block()) {
                is Result.Success -> _snack.postValue("Done")
                is Result.Failure -> _snack.postValue(
                    r.throwable.localizedMessage ?: "Operation failed")
                else -> {}
            }
        }
}

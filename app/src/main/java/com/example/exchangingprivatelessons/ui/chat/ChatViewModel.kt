package com.example.exchangingprivatelessons.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.common.util.mapList
import com.example.exchangingprivatelessons.common.util.toResultLiveData
import com.example.exchangingprivatelessons.domain.model.ViewMessage
import com.example.exchangingprivatelessons.domain.usecase.chat.ObserveChatMessages
import com.example.exchangingprivatelessons.domain.usecase.chat.SendMessage
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    observeMessages: ObserveChatMessages,
    private val sendMessage: SendMessage,
    savedState: SavedStateHandle
) : ViewModel() {

    private val chatId: String = checkNotNull(savedState["chatId"])
    val currentUid: String     = FirebaseAuth.getInstance().currentUser!!.uid

    // ChatViewModel.kt
    val messages: LiveData<Result<List<ViewMessage>>> =
        observeMessages(chatId)               // Flow<Result<List<Message>>>
            .map { res ->
                when (res) {
                    is Result.Success -> Result.Success(
                        res.data.map { ViewMessage.from(it, currentUid) }
                    )
                    is Result.Failure -> res
                    is Result.Loading -> Result.Loading
                }
            }
            .toResultLiveData()



    fun send(text: String) = viewModelScope.launch {
        sendMessage(chatId, null, text)
    }
}

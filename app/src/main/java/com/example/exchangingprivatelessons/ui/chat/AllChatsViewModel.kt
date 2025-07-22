package com.example.exchangingprivatelessons.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exchangingprivatelessons.common.util.Result
import com.example.exchangingprivatelessons.common.util.mapList
import com.example.exchangingprivatelessons.common.util.toResultLiveData
import com.example.exchangingprivatelessons.domain.model.ViewChatPreview
import com.example.exchangingprivatelessons.domain.usecase.chat.ObserveAllChats
import com.example.exchangingprivatelessons.domain.usecase.chat.RefreshAllChats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AllChatsViewModel @Inject constructor(
    observeAllChats: ObserveAllChats,
    private val refreshAllChats: RefreshAllChats
) : ViewModel() {

    val chats: LiveData<Result<List<ViewChatPreview>>> =
        observeAllChats()
            .map { result ->
                // שימוש ישיר בפונקציית העזר שלך. הכי נקי שיש.
                result.mapList { chat ->
                    ViewChatPreview(
                        chatId        = chat.id,
                        peerName      = chat.peerName.orEmpty(),
                        lastMessage   = chat.lastMessage.orEmpty(),
                        lastMessageAt = chat.lastMessageAt
                    )
                }
            }
            .toResultLiveData()

    fun refresh() = viewModelScope.launch { refreshAllChats() }
}
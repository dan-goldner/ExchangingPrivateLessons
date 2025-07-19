package com.example.exchangingprivatelessons.ui.chat

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.exchangingprivatelessons.R
import com.example.exchangingprivatelessons.common.util.*
import com.example.exchangingprivatelessons.common.util.onFailure
import com.example.exchangingprivatelessons.common.util.onSuccess
import com.example.exchangingprivatelessons.databinding.FragmentChatBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import com.example.exchangingprivatelessons.common.util.mapList
import com.example.exchangingprivatelessons.common.util.toResultLiveData


@AndroidEntryPoint
class ChatFragment : Fragment(R.layout.fragment_chat) {

    private val args: ChatFragmentArgs by navArgs()
    private val vm: ChatViewModel by viewModels()

    private val adapter = MessagesAdapter(vm.currentUid)

    private var _b: FragmentChatBinding? = null
    private val b get() = _b!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentChatBinding.bind(view)
        requireActivity().title = args.peerName


        b.messagesRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
            adapter       = this@ChatFragment.adapter
        }

        vm.messages.observe(viewLifecycleOwner) { res ->
            b.progressBar.isVisible = res is com.example.exchangingprivatelessons.common.util.Result.Loading
            res.onSuccess {
                adapter.submitList(it) { b.messagesRecycler.scrollToPosition(adapter.itemCount - 1) }
            }
            res.onFailure {
                Snackbar.make(view, it.localizedMessage ?: "Unknown error", Snackbar.LENGTH_LONG).show()
            }
        }

        b.sendBtn.setOnClickListener {
            val txt = b.messageInput.text.toString().trim()
            if (txt.isNotEmpty()) {
                vm.send(txt)
                b.messageInput.setText("")
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

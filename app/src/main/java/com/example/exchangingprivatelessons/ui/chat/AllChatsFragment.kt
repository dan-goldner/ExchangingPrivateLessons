package com.example.exchangingprivatelessons.ui.chat

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.exchangingprivatelessons.R
import com.example.exchangingprivatelessons.common.util.onSuccess
import com.example.exchangingprivatelessons.common.util.onFailure
import com.example.exchangingprivatelessons.databinding.FragmentAllChatsBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllChatsFragment : Fragment(R.layout.fragment_all_chats) {

    private var _b: FragmentAllChatsBinding? = null
    private val b get() = _b!!

    private val vm: AllChatsViewModel by viewModels()

    private val adapter = ChatPreviewAdapter { chatId, peerName ->
        findNavController().navigate(
            AllChatsFragmentDirections.actionAllChatsFragmentToChatFragment(chatId, peerName)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentAllChatsBinding.bind(view)

        b.chatsRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AllChatsFragment.adapter
        }

        vm.chats.observe(viewLifecycleOwner) { res ->
            b.swipeRefresh.isRefreshing = res is com.example.exchangingprivatelessons.common.util.Result.Loading
            res.onSuccess(adapter::submitList)
            res.onFailure {
                Snackbar.make(view, it.localizedMessage ?: "Unknown error", Snackbar.LENGTH_LONG).show()
            }

        }

        b.swipeRefresh.setOnRefreshListener { vm.refresh() }
    }

    override fun onDestroyView() {
        super.onDestroyView(); _b = null
    }
}

package com.example.exchangingprivatelessons.ui.request

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.exchangingprivatelessons.databinding.FragmentRequestsBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RequestsFragment : Fragment() {

    private var _b: FragmentRequestsBinding? = null
    private val b get() = _b!!
    private val vm by viewModels<RequestsViewModel>()
    private val args by navArgs<RequestsFragmentArgs>()

    private val adapter = RequestAdapter(
        approve = { vm.approve(it) },
        decline = { vm.decline(it) },
        cancel  = { vm.cancel(it)  }
    )

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        FragmentRequestsBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(v: View, s: Bundle?) = with(b) {

        /* מצב הפתיחה שמגיע מ‑SafeArgs */
        vm.setMode(RequestsViewModel.Mode.valueOf(args.mode))

        /* Recycler */
        requestsRv.layoutManager = LinearLayoutManager(requireContext())
        requestsRv.adapter = adapter

        /* Live‑data */
        vm.items.observe(viewLifecycleOwner) { list ->
            emptyTv.isVisible = list.isEmpty()
            adapter.submitList(list)
        }
        vm.snackbar.observe(viewLifecycleOwner) {
            Snackbar.make(root, it, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() { _b = null; super.onDestroyView() }
}

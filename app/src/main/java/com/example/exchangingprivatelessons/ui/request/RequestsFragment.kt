package com.example.exchangingprivatelessons.ui.request

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.example.exchangingprivatelessons.R
import com.example.exchangingprivatelessons.databinding.FragmentRequestsBinding
import com.example.exchangingprivatelessons.databinding.ItemRequestRowBinding
import com.example.exchangingprivatelessons.domain.model.LessonRequest
import com.example.exchangingprivatelessons.domain.model.RequestStatus
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RequestsFragment : Fragment() {

    private var _vb: FragmentRequestsBinding? = null
    private val vb get() = _vb!!
    private val vm by viewModels<RequestsViewModel>()
    private val args by navArgs<RequestsFragmentArgs>()

    private val adapter = RequestAdapter { req ->
        RequestDetailSheet.newInstance(req).show(childFragmentManager, "req_sheet")
    }

    override fun onCreateView(
        i: LayoutInflater, c: ViewGroup?, s: Bundle?
    ) = FragmentRequestsBinding.inflate(i, c, false)
        .also { _vb = it }
        .root

    override fun onViewCreated(v: View, s: Bundle?) = with(vb) {

        /* מצב התחלתי (Safe‑Args) */
        vm.setMode(RequestsViewModel.Mode.valueOf(args.mode))

        /* Recycler */
        requestsRv.layoutManager = LinearLayoutManager(requireContext())
        requestsRv.adapter = adapter

        /* Toggle mode */
        modeToggle.addOnButtonCheckedListener { _, id, checked ->
            if (!checked) return@addOnButtonCheckedListener
            val m = if (id == R.id.btnReceived)
                RequestsViewModel.Mode.RECEIVED else RequestsViewModel.Mode.SENT
            vm.setMode(m)
            statusChips.isVisible = m == RequestsViewModel.Mode.SENT
        }

        /* Status chips (רק למצב SENT) */
        statusChips.setOnCheckedStateChangeListener { _, ids ->
            val st = when (ids.first()) {
                R.id.chip_pending  -> RequestStatus.Pending
                R.id.chip_approved -> RequestStatus.Approved
                else               -> RequestStatus.Declined
            }
            vm.setStatus(st)
        }

        /* LiveData */
        vm.requests.observe(viewLifecycleOwner) { list ->
            emptyTv.isVisible = list.isEmpty()
            adapter.submitList(list)
        }
        vm.snackbar.observe(viewLifecycleOwner) {
            Snackbar.make(root, it, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() { _vb = null; super.onDestroyView() }

    /* ---------- Adapter ---------- */
    private class RequestAdapter(
        val click: (LessonRequest) -> Unit
    ) : ListAdapter<LessonRequest, RequestAdapter.VH>(Diff) {

        override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(
            ItemRequestRowBinding.inflate(LayoutInflater.from(p.context), p, false)
        )

        override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos), click)

        class VH(private val b: ItemRequestRowBinding)
            : androidx.recyclerview.widget.RecyclerView.ViewHolder(b.root) {

            fun bind(r: LessonRequest, click: (LessonRequest) -> Unit) = with(b) {
                titleTv.text = r.lessonId
                subtitleTv.text = "Owner: ${r.ownerId}\nRequested: ${r.requestedAt}"
                statusTv.text = r.status.name
                root.setOnClickListener { click(r) }
            }
        }

        private object Diff : DiffUtil.ItemCallback<LessonRequest>() {
            override fun areItemsTheSame(a: LessonRequest, b: LessonRequest) = a.id == b.id
            override fun areContentsTheSame(a: LessonRequest, b: LessonRequest) = a == b
        }
    }
}

package com.example.exchangingprivatelessons.ui.request

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import coil.load
import com.example.exchangingprivatelessons.R
import com.example.exchangingprivatelessons.databinding.FragmentRequestsBinding
import com.example.exchangingprivatelessons.databinding.ItemRequestRowBinding
import com.example.exchangingprivatelessons.domain.model.LessonRequest
import com.example.exchangingprivatelessons.domain.model.RequestStatus
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RequestsFragment : Fragment() {

    private var _binding: FragmentRequestsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RequestsViewModel by viewModels()
    private val adapter = RequestAdapter { req ->
        RequestDetailSheet.newInstance(req).show(childFragmentManager, "req_sheet")
    }

    override fun onCreateView(
        i: LayoutInflater, c: ViewGroup?, s: Bundle?
    ): View = FragmentRequestsBinding.inflate(i, c, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.requestsRv.layoutManager = LinearLayoutManager(requireContext())
        binding.requestsRv.adapter = adapter

        /* שינוי מצב ע״פ Chip */
        binding.statusChips.setOnCheckedStateChangeListener { _, checkedIds ->
            val status = when (checkedIds.first()) {
                R.id.chip_pending  -> RequestStatus.Pending
                R.id.chip_approved -> RequestStatus.Approved
                else               -> RequestStatus.Declined
            }
            viewModel.setStatus(status)
        }

        /* ---- LiveData observers ---- */
        viewModel.snackbar.observe(viewLifecycleOwner) { msg ->
            Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
        }

        viewModel.requests.observe(viewLifecycleOwner) { list ->
            binding.emptyTv.isVisible = list.isEmpty()
            adapter.submitList(list)
        }
    }


    override fun onDestroyView() { _binding = null; super.onDestroyView() }

    /* ---------- list adapter ---------- */

    private class RequestAdapter(
        val onClick: (LessonRequest) -> Unit
    ) : ListAdapter<LessonRequest, RequestAdapter.VH>(Diff) {

        override fun onCreateViewHolder(p: ViewGroup, t: Int) =
            VH(ItemRequestRowBinding.inflate(LayoutInflater.from(p.context), p, false))

        override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos), onClick)

        class VH(private val b: ItemRequestRowBinding) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(b.root) {

            fun bind(req: LessonRequest, click: (LessonRequest) -> Unit) = with(b) {
                titleTv.text = req.lessonId     // later map lessonId → title if you cache lessons
                subtitleTv.text = "Owner: ${req.ownerId}\nRequested: ${req.requestedAt}"
                statusTv.text = req.status.name
                root.setOnClickListener { click(req) }
            }
        }

        private object Diff : DiffUtil.ItemCallback<LessonRequest>() {
            override fun areItemsTheSame(a: LessonRequest, b: LessonRequest) = a.lessonId == b.lessonId
            override fun areContentsTheSame(a: LessonRequest, b: LessonRequest) = a == b
        }
    }
}

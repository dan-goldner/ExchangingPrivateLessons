package com.example.exchangingprivatelessons.ui.request

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import coil.load
import com.example.exchangingprivatelessons.databinding.BottomSheetRequestDetailBinding
import com.example.exchangingprivatelessons.domain.model.LessonRequest
import com.example.exchangingprivatelessons.domain.model.RequestStatus
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable

@AndroidEntryPoint
class RequestDetailSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetRequestDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RequestsViewModel by activityViewModels()

    private val request by lazy {
        requireArguments().getSerializable(ARG_REQ) as LessonRequest
    }

    override fun onCreateView(
        inf: LayoutInflater, c: ViewGroup?, s: Bundle?
    ): View = BottomSheetRequestDetailBinding.inflate(inf, c, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(v: View, s: Bundle?) = with(binding) {
        titleTv.text    = request.lessonId
        subtitleTv.text = "Requester: ${request.requesterId}\nOwner: ${request.ownerId}"
        dateTv.text     = request.requestedAt.toString()

        when (request.status) {
            RequestStatus.Pending -> {
                approveBtn.setOnClickListener { viewModel.approve(requestId()); dismiss() }
                declineBtn.setOnClickListener { viewModel.decline(requestId()); dismiss() }
                cancelBtn.setOnClickListener  { viewModel.cancel(requestId());  dismiss() }
            }
            else -> {
                approveBtn.isEnabled = false
                declineBtn.isEnabled = false
                cancelBtn.isEnabled  = false
            }
        }
    }

    private fun requestId(): String = request.id   // `id` must exist in the Serializable object

    override fun onDestroyView() { _binding = null; super.onDestroyView() }

    companion object {
        private const val ARG_REQ = "req"
        fun newInstance(req: LessonRequest) =
            RequestDetailSheet().apply { arguments = bundleOf(ARG_REQ to req as Serializable) }
    }
}

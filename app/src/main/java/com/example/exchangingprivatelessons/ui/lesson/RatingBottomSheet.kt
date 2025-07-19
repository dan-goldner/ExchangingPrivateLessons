package com.example.exchangingprivatelessons.ui.lesson

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.exchangingprivatelessons.databinding.BottomSheetRatingBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RatingBottomSheet : BottomSheetDialogFragment() {

    private var _vb: BottomSheetRatingBinding? = null
    private val vb get() = _vb!!

    private val vm   by viewModels<RatingViewModel>()
    private val args by navArgs<RatingBottomSheetArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = BottomSheetRatingBinding.inflate(inflater, container, false)
        .also { _vb = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(vb) {

        vm.init(args.lessonId)   // ⬅️ מעביר lessonId ל‑VM

        ratingBar.setOnRatingBarChangeListener { _, value, _ ->
            vm.setRating(value.toInt())
        }
        commentEt.doAfterTextChanged { vm.setComment(it.toString()) }

        saveBtn.setOnClickListener {
            vm.save { ok ->
                if (ok) {
                    setFragmentResult(RESULT_KEY, Bundle.EMPTY)
                    dismiss()
                }
            }
        }
    }

    override fun onDestroyView() { _vb = null; super.onDestroyView() }

    companion object { const val RESULT_KEY = "rating_saved" }
}

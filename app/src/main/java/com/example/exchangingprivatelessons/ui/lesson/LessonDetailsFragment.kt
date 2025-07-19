package com.example.exchangingprivatelessons.ui.lesson

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.exchangingprivatelessons.R
import com.example.exchangingprivatelessons.common.util.toRelativeTime
import com.example.exchangingprivatelessons.databinding.FragmentLessonDetailsBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LessonDetailsFragment : Fragment(), MenuProvider {

    private var _vb: FragmentLessonDetailsBinding? = null
    private val vb get() = _vb!!

    private val vm by viewModels<LessonDetailsViewModel>()

    /** ① קבלה בטוחה של הפרמטר */
    private val args by navArgs<LessonDetailsFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, state: Bundle?
    ) = FragmentLessonDetailsBinding.inflate(inflater, container, false)
        .also { _vb = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        requireActivity().addMenuProvider(this, viewLifecycleOwner)

        /* ② מסירים את ה-ARG הקבוע – משתמשים ב-Safe-Args */
        val lessonId = args.lessonId
        vm.loadLesson(lessonId)

        vb.requestBtn.setOnClickListener { vm.onRequestLesson() }
        vb.rateBtn.setOnClickListener {
            RatingBottomSheet().apply {
                arguments = Bundle().apply {
                    putString("lessonId", args.lessonId)
                }
            }.show(parentFragmentManager, null)

        }

        /* BottomSheets */
        setFragmentResultListener(RatingBottomSheet.RESULT_KEY) { _, _ -> vm.refresh() }
        setFragmentResultListener(AddEditLessonFragment.RESULT_KEY) { _, _ -> vm.refresh() }

        /* LiveData → UI */
        vm.state.observe(viewLifecycleOwner) { bindState(it) }
    }

    private fun bindState(state: LessonDetailsViewModel.DetailsState) = with(vb) {
        progressBar.isVisible = state.loading
        state.lesson?.let { lesson ->
            collapsingToolbar.title = lesson.title
            lessonImg.load(lesson.imageUrl)
            descTv.text = lesson.description
            metaTv.text = getString(
                R.string.lesson_meta_full,
                lesson.ownerName,
                lesson.createdAt.toRelativeTime(requireContext()),
                lesson.ratingAvg,
                lesson.ratingCount
            )
            rateBtn.isVisible    = state.canRate
            requestBtn.isVisible = state.canRequest
        }
        state.errorMsg?.let { Snackbar.make(root, it, Snackbar.LENGTH_LONG).show() }
        requireActivity().invalidateMenu()
    }

    /* ─── Toolbar ─── */

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_lesson_details, menu)
        menu.findItem(R.id.action_edit).isVisible = vm.state.value?.canEdit == true
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_edit -> {
            /* ③ ניווט בטוח עם ארגומנטים טיפוסיים */
            val action =
                LessonDetailsFragmentDirections
                    .actionLessonDetailsFragmentToAddEditLessonFragment(args.lessonId)
            findNavController().navigate(action)
            true
        }
        else -> false
    }

    override fun onDestroyView() { _vb = null; super.onDestroyView() }
}

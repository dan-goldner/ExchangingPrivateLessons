package com.example.exchangingprivatelessons.ui.lesson

import android.os.Bundle
import android.view.*
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
    private val args by navArgs<LessonDetailsFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, state: Bundle?
    ) = FragmentLessonDetailsBinding.inflate(inflater, container, false)
        .also { _vb = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        /* טוען את השיעור לפי Safe‑Args */
        vm.loadLesson(args.lessonId)

        /* כפתורים */
        vb.requestBtn.setOnClickListener { vm.onRequestLesson() }
        vb.rateBtn.setOnClickListener {
            val action = LessonDetailsFragmentDirections
                .actionLessonDetailsFragmentToRatingBottomSheet(args.lessonId)
            findNavController().navigate(action)
        }

        /* BottomSheet results */
        setFragmentResultListener(RatingBottomSheet.RESULT_KEY) { _, _ -> vm.refresh() }
        setFragmentResultListener(AddEditLessonFragment.RESULT_KEY) { _, _ -> vm.refresh() }

        vm.state.observe(viewLifecycleOwner, ::bind)
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
    }

    private fun bind(state: LessonDetailsViewModel.DetailsState) = with(vb) {
        progressBar.isVisible = state.loading

        state.lesson?.let { lesson ->
            // Action‑Bar ראשי של האפליקציה
            (requireActivity() as AppCompatActivity).supportActionBar?.title = lesson.title

            ownerAvatar.load(lesson.ownerPhotoUrl) {
                placeholder(R.drawable.ic_profile_placeholder)
                error(R.drawable.ic_profile_placeholder)
                crossfade(true)
            }
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

            ownerAvatar.apply {
                strokeWidth = resources.getDimensionPixelSize(R.dimen.space_xs).toFloat()
                strokeColor = ContextCompat.getColorStateList(context, R.color.md_theme_primary)
            }

        }

        state.errorMsg?.let { Snackbar.make(root, it, Snackbar.LENGTH_LONG).show() }
        requireActivity().invalidateMenu()
    }

    /* ─── Options‑menu (עריכת שיעור) ─── */

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_lesson_details, menu)
        menu.findItem(R.id.action_edit).isVisible = vm.state.value?.canEdit == true
    }

    override fun onMenuItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_edit -> {
            val action = LessonDetailsFragmentDirections
                .actionLessonDetailsFragmentToAddEditLessonFragment(args.lessonId)
            findNavController().navigate(action); true
        }
        else -> false
    }

    override fun onDestroyView() { _vb = null; super.onDestroyView() }
}

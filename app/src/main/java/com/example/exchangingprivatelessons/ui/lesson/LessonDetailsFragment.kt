package com.example.exchangingprivatelessons.ui.lesson

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
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

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentLessonDetailsBinding.inflate(i, c, false).also { _vb = it }.root

    override fun onViewCreated(v: View, s: Bundle?) {

        vm.loadLesson(args.lessonId)

        vb.requestBtn.setOnClickListener { vm.onRequestLesson() }
        vb.rateBtn.setOnClickListener {
            findNavController().navigate(
                LessonDetailsFragmentDirections
                    .actionLessonDetailsFragmentToRatingBottomSheet(args.lessonId))
        }

        setFragmentResultListener(RatingBottomSheet.RESULT_KEY) { _, _ -> vm.refresh() }
        setFragmentResultListener(AddEditLessonFragment.RESULT_KEY) { _, _ -> vm.refresh() }

        vm.state.observe(viewLifecycleOwner, ::bind)
        vm.snackbar.observe(viewLifecycleOwner) { msg ->
            msg?.let { Snackbar.make(vb.root, it, Snackbar.LENGTH_LONG).show(); vm.snackbarShown() }
        }

        requireActivity().addMenuProvider(this, viewLifecycleOwner)
    }

    /* ---------- UI‑binding ---------- */
    private fun bind(st: LessonDetailsViewModel.DetailsState) = with(vb) {

        progressBar.isVisible = st.loading

        st.lesson?.let { lesson ->
            (requireActivity() as AppCompatActivity)
                .supportActionBar?.title = lesson.title

            /* Avatar */
            ownerAvatar.load(lesson.ownerPhotoUrl) {
                placeholder(R.drawable.ic_profile_placeholder)
                error(R.drawable.ic_profile_placeholder)
                crossfade(true)
            }
            ownerAvatar.strokeWidth =
                resources.getDimensionPixelSize(R.dimen.space_xs).toFloat()
            ownerAvatar.strokeColor =
                ContextCompat.getColorStateList(requireContext(), R.color.md_theme_primary)

            /* Texts */
            descTv.text = lesson.description
            metaTv.text = getString(
                R.string.lesson_meta_full,
                lesson.ownerName,
                lesson.createdAt.toRelativeTime(requireContext()),
                lesson.ratingAvg,
                lesson.ratingCount
            )
        }

        /* בקשה */
        when {
            st.pending -> {
                requestBtn.apply {
                    isVisible = true
                    isEnabled = false
                    text = getString(R.string.request_sent)
                }
            }
            st.canRequest -> {
                requestBtn.apply {
                    isVisible = true
                    isEnabled = true
                    text = getString(R.string.request)
                }
            }
            else -> requestBtn.isVisible = false
        }

        /* דירוג */
        rateBtn.isVisible = st.canRate
        requireActivity().invalidateMenu()
    }

    /* ---------- Options‑menu (עריכת שיעור) ---------- */

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_lesson_details, menu)
        menu.findItem(R.id.action_edit).isVisible = vm.state.value?.canEdit == true
    }

    override fun onMenuItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_edit -> {
            findNavController().navigate(
                LessonDetailsFragmentDirections
                    .actionLessonDetailsFragmentToAddEditLessonFragment(args.lessonId)
            ); true
        }
        else -> false
    }

    override fun onDestroyView() { _vb = null; super.onDestroyView() }
}

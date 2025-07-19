package com.example.exchangingprivatelessons.ui.lesson

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import coil.load
import com.example.exchangingprivatelessons.R
import com.example.exchangingprivatelessons.databinding.FragmentLessonListBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LessonListFragment : Fragment() {

    private var _vb: FragmentLessonListBinding? = null
    private val vb get() = _vb!!

    private val vm   by viewModels<LessonListViewModel>()
    private val args by navArgs<LessonListFragmentArgs>()

    private val adapter = LessonAdapter(::onLessonClicked, ::onArchiveClicked)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentLessonListBinding.inflate(inflater, container, false)
        .also { _vb = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(vb) {

        lessonRv.adapter = adapter
        lessonRv.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )

        /* מצב ראשוני – Safe‑Args */
        vm.setMode(LessonListViewModel.Mode.valueOf(args.mode))
        selectChip(args.mode)

        modeChips.setOnCheckedStateChangeListener { _, ids ->
            val mode = when (ids.first()) {
                R.id.chip_my    -> LessonListViewModel.Mode.MINE
                R.id.chip_taken -> LessonListViewModel.Mode.TAKEN
                else            -> LessonListViewModel.Mode.AVAILABLE
            }
            vm.setMode(mode)
        }

        swipeRefresh.setOnRefreshListener { vm.onRefresh() }
        addFab.setOnClickListener {
            findNavController().navigate(
                LessonListFragmentDirections.actionLessonListToAddEditLesson("") // מחרוזת ריקה במקום null
            )
        }

        parentFragmentManager.setFragmentResultListener(
            AddEditLessonFragment.RESULT_KEY, viewLifecycleOwner
        ) { _, _ -> vm.onRefresh() }

        vm.uiState.observe(viewLifecycleOwner) { s ->
            adapter.submitList(s.lessons)
            emptyTv.isVisible         = s.lessons.isEmpty() && !s.loading
            progressBar.isVisible     = s.loading
            swipeRefresh.isRefreshing = s.refreshing

            s.errorMsg?.let {
                Snackbar.make(root, it, Snackbar.LENGTH_LONG).show()
                vm.errorShown()
            }
        }
    }

    private fun selectChip(mode: String) = vb.modeChips.check(
        when (LessonListViewModel.Mode.valueOf(mode)) {
            LessonListViewModel.Mode.MINE      -> R.id.chip_my
            LessonListViewModel.Mode.TAKEN     -> R.id.chip_taken
            LessonListViewModel.Mode.AVAILABLE -> R.id.chip_available
        }
    )

    private fun onLessonClicked(id: String) =
        findNavController().navigate(
            LessonListFragmentDirections.actionLessonListToLessonDetails(id)
        )

    private fun onArchiveClicked(id: String, archived: Boolean) =
        vm.onArchiveToggle(id, archived)

    override fun onDestroyView() { _vb = null; super.onDestroyView() }
}

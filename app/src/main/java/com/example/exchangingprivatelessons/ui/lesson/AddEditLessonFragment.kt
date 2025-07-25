package com.example.exchangingprivatelessons.ui.lesson

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.exchangingprivatelessons.R
import com.example.exchangingprivatelessons.common.util.pretty
import com.example.exchangingprivatelessons.databinding.FragmentAddEditLessonBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEditLessonFragment : Fragment() {

    private var _b: FragmentAddEditLessonBinding? = null
    private val b get() = _b!!

    private val args: AddEditLessonFragmentArgs by navArgs()
    private val vm: AddEditLessonViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentAddEditLessonBinding.inflate(inflater, container, false)
        .also { _b = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        /* UI events */
        b.saveBtn.setOnClickListener {
            vm.onSaveClicked(
                title       = b.titleEt.text.toString(),
                description = b.descEt.text.toString()
            )
        }
        // Show delete button only when editing an existing lesson
        if (!args.lessonId.isNullOrEmpty()) {
            b.deleteBtn.visibility = View.VISIBLE

            b.deleteBtn.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.delete_lesson)
                    .setMessage(R.string.delete_lesson_confirm)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        vm.deleteLesson()     // ← קריאה
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()

            }
        }

        /* collect LiveData */
        vm.ui.observe(viewLifecycleOwner, ::render)
    }

    private fun render(state: AddEditUiState) = with(b) {
        progressBar.isVisible   = state.loading
        saveBtn.isEnabled       = !state.loading

        /* שגיאה */
        errorTv.isVisible       = state.errorMsg != null
        errorTv.text            = state.errorMsg

        /* בשלב טעינת שיעור קיים */


        /*state.existingLesson?.let { lesson ->
            if (args.lessonId != null) {
                titleEt.setText(lesson.title)
                descEt.setText(lesson.description)
            }

            metaTv.apply {
                text = getString(
                    R.string.lesson_meta,
                    lesson.createdAt.pretty(),
                    lesson.ratingCount
                )
                isVisible = true
            }
        }*/

        if (!args.lessonId.isNullOrEmpty() && state.existingLesson != null) {
            val lesson = state.existingLesson
            titleEt.setText(lesson?.title.orEmpty())
            descEt.setText(lesson?.description.orEmpty())
            metaTv.apply {
                text = getString(
                    R.string.lesson_meta,
                    lesson?.createdAt?.pretty().orEmpty(),
                    lesson?.ratingCount ?: 0
                )
                isVisible = true
            }
        }

        /* הצלחנו לשמור? נחזיר תשובה */
        /*state.savedLessonId?.let {
            setFragmentResult(RESULT_KEY, Bundle().apply { putString(RESULT_ID, it) })
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }*/

        state.savedLessonId?.let {
            if (state.justDeleted) {
                Snackbar.make(b.root, R.string.lesson_deleted_successfully, Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(b.root, R.string.lesson_saved_successfully, Snackbar.LENGTH_SHORT).show()
            }

            Handler(Looper.getMainLooper()).postDelayed({
                setFragmentResult(RESULT_KEY, Bundle().apply { putString(RESULT_ID, it) })

                // 🧭 This will go back to the fragment with the given destination ID (e.g., LessonListFragment)
                findNavController().popBackStack(R.id.lessonListFragment,false)
            }, 1000)
        }
    }

    override fun onDestroyView() { _b = null; super.onDestroyView() }

    companion object {
        const val RESULT_KEY = "add_edit_result"
        const val RESULT_ID  = "lessonId"
    }
}

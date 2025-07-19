package com.example.exchangingprivatelessons.ui.lesson

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.exchangingprivatelessons.R
import com.example.exchangingprivatelessons.common.util.pretty
import com.example.exchangingprivatelessons.databinding.FragmentAddEditLessonBinding
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

        /* back‑arrow */
        b.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        /* UI events */
        b.saveBtn.setOnClickListener {
            vm.onSaveClicked(
                title       = b.titleEt.text.toString(),
                description = b.descEt.text.toString()
            )
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
        state.existingLesson?.let { lesson ->
            if (titleEt.text?.isEmpty() == true) titleEt.setText(lesson.title)
            if (descEt.text?.isEmpty() == true)  descEt.setText(lesson.description)
            metaTv.apply {
                text = getString(
                    R.string.lesson_meta,
                    lesson.createdAt.pretty(),
                    lesson.ratingCount
                )
                isVisible = true
            }
        }

        /* הצלחנו לשמור? נחזיר תשובה */
        state.savedLessonId?.let {
            setFragmentResult(RESULT_KEY, Bundle().apply { putString(RESULT_ID, it) })
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() { _b = null; super.onDestroyView() }

    companion object {
        const val RESULT_KEY = "add_edit_result"
        const val RESULT_ID  = "lessonId"
    }
}

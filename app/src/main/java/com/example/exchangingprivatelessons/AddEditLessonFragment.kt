package com.example.exchangingprivatelessons

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.exchangingprivatelessons.databinding.FragmentAddEditLessonBinding
import com.example.exchangingprivatelessons.model.Lesson
import com.example.exchangingprivatelessons.viewmodel.LessonViewModel
import androidx.navigation.fragment.findNavController


class AddEditLessonFragment : Fragment(R.layout.fragment_add_edit_lesson) {
    private var _b: FragmentAddEditLessonBinding? = null
    private val b get() = _b!!
    private val vm: LessonViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentAddEditLessonBinding.bind(view)
        b.buttonAddLesson.setOnClickListener {
            val title = b.editTextTitle.text.toString().trim()
            val desc = b.editTextDescription.text.toString().trim()
            if (title.isNotEmpty() && desc.isNotEmpty()) {
                vm.addLesson(Lesson(title = title, description = desc))
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Fill in fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}

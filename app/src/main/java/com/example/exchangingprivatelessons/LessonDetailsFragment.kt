package com.example.exchangingprivatelessons

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.exchangingprivatelessons.databinding.FragmentLessonDetailsBinding

class LessonDetailsFragment : Fragment(R.layout.fragment_lesson_details) {

    private var _b: FragmentLessonDetailsBinding? = null
    private val b get() = _b!!
    private val args: LessonDetailsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentLessonDetailsBinding.bind(view)
        Log.d("LessonDetailsFragment", "onViewCreated")

        b.textTitle.text       = args.title
        b.textDescription.text = args.description

        b.buttonChat.setOnClickListener {
            findNavController()
                .navigate(R.id.action_lessonDetailsFragment_to_chatFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}

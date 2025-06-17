package com.example.exchangingprivatelessons

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.appcompat.app.AppCompatActivity
import com.example.exchangingprivatelessons.databinding.FragmentLessonDetailsBinding
import androidx.navigation.fragment.findNavController


class LessonDetailsFragment : Fragment(R.layout.fragment_lesson_details) {
    private var _b: FragmentLessonDetailsBinding? = null
    private val b get() = _b!!
    private val args: LessonDetailsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentLessonDetailsBinding.bind(view)
        Log.d("LessonDetailsFragment", "onViewCreated: Lesson Details Fragment displayed.")
        b.textTitle.text = args.title
        b.textDescription.text = args.description

        b.buttonChat.setOnClickListener {
            // לצורך הממשק – נווט לצ׳אט (לא מימשנו בפועל)
            findNavController().navigate(R.id.action_lessonDetailsFragment_to_chatFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        // השורה הבאה מיותרת וגורמת לשגיאת compile כי הפונקציה כבר לא קיימת
        //(activity as? MainActivity)?.updateToolbar()

        // אם אתה עדיין רוצה לוודא שה-Toolbar גלוי - זה מספיק:
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}

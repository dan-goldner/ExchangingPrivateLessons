package com.example.exchangingprivatelessons

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.exchangingprivatelessons.adapter.LessonsAdapter
import com.example.exchangingprivatelessons.databinding.FragmentMyLessonsBinding
import com.example.exchangingprivatelessons.viewmodel.LessonViewModel
import com.google.firebase.auth.FirebaseAuth

class MyLessonsFragment : Fragment(R.layout.fragment_my_lessons) {

    private var _b: FragmentMyLessonsBinding? = null
    private val b get() = _b!!
    private val vm: LessonViewModel by viewModels()
    private lateinit var adapter: LessonsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentMyLessonsBinding.bind(view)

        /* RecyclerView + Fab */
        adapter = LessonsAdapter(emptyList()) { lesson ->
            val action = MyLessonsFragmentDirections
                .actionMyLessonsFragmentToLessonDetailsFragment(
                    lesson.id, lesson.title, lesson.description
                )
            findNavController().navigate(action)
        }

        b.recyclerViewLessons.layoutManager = LinearLayoutManager(requireContext())
        b.recyclerViewLessons.adapter       = adapter

        vm.lessons.observe(viewLifecycleOwner) { adapter.updateLessons(it) }

        b.fabAddLesson.setOnClickListener {
            findNavController()
                .navigate(R.id.action_myLessonsFragment_to_addEditLessonFragment)
        }

        /* MenuProvider */
        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
                    inflater.inflate(R.menu.menu_main, menu)
                }

                override fun onMenuItemSelected(item: MenuItem): Boolean =
                    when (item.itemId) {
                        R.id.action_logout -> {
                            FirebaseAuth.getInstance().signOut()
                            findNavController().navigate(R.id.loginFragment)
                            true
                        }
                        else -> false
                    }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}

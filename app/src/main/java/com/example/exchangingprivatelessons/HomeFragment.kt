package com.example.exchangingprivatelessons

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider          // ★ חדש
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle            // ★ חדש
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.exchangingprivatelessons.adapter.LessonsAdapter
import com.example.exchangingprivatelessons.databinding.FragmentHomeBinding
import com.example.exchangingprivatelessons.viewmodel.LessonViewModel
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!
    private val vm: LessonViewModel by viewModels()
    private lateinit var adapter: LessonsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentHomeBinding.bind(view)

        /* --- RecyclerView + Fab (ללא שינוי) --- */
        adapter = LessonsAdapter(emptyList()) { lesson ->
            val action = HomeFragmentDirections
                .actionHomeFragmentToLessonDetailsFragment(
                    lesson.id, lesson.title, lesson.description
                )
            findNavController().navigate(action)
        }
        b.recyclerViewLessons.layoutManager = LinearLayoutManager(requireContext())
        b.recyclerViewLessons.adapter = adapter

        vm.lessons.observe(viewLifecycleOwner) { adapter.updateLessons(it) }

        b.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addEditLessonFragment)
        }

        /* --- MenuProvider חדש --- */
        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
                    inflater.inflate(R.menu.menu_main, menu)
                }

                override fun onMenuItemSelected(item: MenuItem): Boolean =
                    when (item.itemId) {
                        R.id.action_logout -> {
                            FirebaseAuth.getInstance().signOut()
                            findNavController()
                                .navigate(R.id.action_homeFragment_to_loginFragment)
                            true
                        }
                        else -> false
                    }
            },
            viewLifecycleOwner,            // קושר למחזור-חיים של הפרגמנט
            Lifecycle.State.RESUMED        // מפעיל רק כשהוא במצב RESUMED
        )
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}

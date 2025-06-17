package com.example.exchangingprivatelessons

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // מאפשר הצגת תפריט ב־Toolbar
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentHomeBinding.bind(view)

        adapter = LessonsAdapter(emptyList()) { lesson ->
            val action = HomeFragmentDirections
                .actionHomeFragmentToLessonDetailsFragment(
                    lesson.id,
                    lesson.title,
                    lesson.description
                )
            findNavController().navigate(action)
        }

        b.recyclerViewLessons.layoutManager = LinearLayoutManager(requireContext())
        b.recyclerViewLessons.adapter = adapter

        vm.lessons.observe(viewLifecycleOwner) {
            adapter.updateLessons(it)
        }

        b.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addEditLessonFragment)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                val action = HomeFragmentDirections.actionHomeFragmentToLoginFragment()
                findNavController().navigate(action)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
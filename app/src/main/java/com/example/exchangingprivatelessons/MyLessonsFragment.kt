package com.example.exchangingprivatelessons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.exchangingprivatelessons.adapter.LessonsAdapter
import com.example.exchangingprivatelessons.databinding.FragmentMyLessonsBinding
import com.example.exchangingprivatelessons.viewmodel.LessonViewModel


class MyLessonsFragment : Fragment() {

    private var _binding: FragmentMyLessonsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LessonViewModel by viewModels()
    private lateinit var adapter: LessonsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyLessonsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                findNavController().navigate(R.id.loginFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = LessonsAdapter(emptyList()) { lesson ->
            val action = MyLessonsFragmentDirections
                .actionMyLessonsFragmentToLessonDetailsFragment(
                    lesson.id,
                    lesson.title,
                    lesson.description
                )
            findNavController().navigate(action)
        }

        binding.recyclerViewLessons.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewLessons.adapter = adapter

        viewModel.lessons.observe(viewLifecycleOwner) { lessons ->
            adapter.updateLessons(lessons)
        }

        binding.fabAddLesson.setOnClickListener {
            val action = MyLessonsFragmentDirections.actionMyLessonsFragmentToAddEditLessonFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// app/src/main/java/com/example/exchangingprivatelessons/ui/home/HomeFragment.kt
package com.example.exchangingprivatelessons.ui.home

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.exchangingprivatelessons.R
import com.example.exchangingprivatelessons.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!

    private val vm: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentHomeBinding.bind(view)

        /* ───────────── Navigation ───────────── */
        b.btnMyLessons.setOnClickListener   {
            val dir = HomeFragmentDirections.actionHomeToLessonListMine()
            findNavController().navigate(dir)
        }

        b.btnAvailabaleLessons.setOnClickListener   {
            val dir = HomeFragmentDirections.actionHomeToLessonListAvailable()
            findNavController().navigate(dir)
        }

        b.btnRequests.setOnClickListener  {
            val dir = HomeFragmentDirections.actionHomeFragmentToRequestsFragment()
            findNavController().navigate(dir)
        }
        b.btnChats.setOnClickListener     {
            val dir = HomeFragmentDirections.actionHomeFragmentToAllChatsFragment()
            findNavController().navigate(dir)
        }
        b.btnProfile.setOnClickListener   {
            val dir = HomeFragmentDirections.actionHomeFragmentToProfileFragment()
            findNavController().navigate(dir)
        }

        /* ───────────── State ───────────── */
        vm.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is HomeUiState.Loading  -> showLoading(true)
                is HomeUiState.Content  -> {
                    showLoading(false)
                    // דוגמה להצגת נתונים במסך הבית
                    b.txtLessonsCount.text   = state.lessons.size.toString()
                    b.txtRequestsCount.text  = state.incoming.size.toString()
                }
                is HomeUiState.Error    -> {
                    showLoading(false)
                    Snackbar.make(b.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        b.swipeRefresh.setOnRefreshListener { vm.refresh() }
    }

    private fun showLoading(loading: Boolean) {
        b.progressBar.isVisible   = loading
        b.contentGroup.isVisible  = !loading
        b.swipeRefresh.isRefreshing = loading
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

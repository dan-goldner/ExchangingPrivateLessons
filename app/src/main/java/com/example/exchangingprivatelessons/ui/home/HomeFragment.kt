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
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!

    private val vm by viewModels<HomeViewModel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentHomeBinding.bind(view)

        /* ----- Greeting (Reactive) ----- */
        vm.userName.observe(viewLifecycleOwner) { name ->        // 🔵
            val finalName =
                name.ifBlank {                                   // אם עדיין ריק …
                    FirebaseAuth.getInstance().currentUser
                        ?.email?.substringBefore('@') ?: "there" // fallback
                }
            b.txtGreeting.text = getString(R.string.greeting, finalName)
        }

        /* ───── כפתורי ניווט ───── */

        // שיעורים זמינים
        b.btnAvailableLessons.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeToLessonListAvailable()
            )
        }

        // שיעורים שנרשמתי אליהם
        b.btnMyLessons.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeToLessonListTaken()
            )
        }

        // שיעורים שאני מציע
        b.btnMyOfferedLessons.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeToLessonListMine()
            )
        }

        // בקשות שקיבלתי
        b.btnRequestsToMe.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeToRequestsReceived()
            )
        }

        // בקשות ששלחתי
        b.btnMyRequests.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeToRequestsSent()
            )
        }

        // צ'אטים
    /*    b.btnChats.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToAllChatsFragment()
            )
        }
    */
        // פרופיל
        b.btnProfile.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToProfileFragment(null)
            )
        }


        /* ───────────── State ───────────── */
        vm.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is HomeUiState.Loading -> showLoading(true)

                is HomeUiState.Content -> {
                    showLoading(false)

                    // ℹ️ ה‑Content מכיל כבר את הרשימות
                    val lessonsCount   = state.lessons.size
                    val requestsCount  = state.incoming.size

                    // Chip texts (שמאל‑לימין: מספר ואז טקסט)
                    b.txtLessonsCount.text  = "$lessonsCount שיעורים זמינים"
                    b.txtRequestsCount.text = "$requestsCount בקשות נכנסות"
                }

                is HomeUiState.Error -> {
                    showLoading(false)
                    Snackbar.make(b.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        b.swipeRefresh.setOnRefreshListener { vm.refresh() }


        b.swipeRefresh.setOnRefreshListener { vm.refresh() }
    }


    private fun showLoading(loading: Boolean) {
        b.progressBar.isVisible   = loading
        b.contentGroup.isVisible  = !loading
        b.swipeRefresh.isRefreshing = loading
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

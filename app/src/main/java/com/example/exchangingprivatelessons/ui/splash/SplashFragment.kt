package com.example.exchangingprivatelessons.ui.splash

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.exchangingprivatelessons.R
import com.example.exchangingprivatelessons.databinding.FragmentSplashBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : Fragment(R.layout.fragment_splash) {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SplashViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("SplashFragment", "onViewCreated called")
        _binding = FragmentSplashBinding.bind(view)

        viewModel.authState.observe(viewLifecycleOwner) { isLoggedIn ->
            Log.d("SplashFragment", "authState: $isLoggedIn")
            val action = if (isLoggedIn)
                SplashFragmentDirections.actionSplashToHome()
            else
                SplashFragmentDirections.actionSplashToAuth()

            // מבטיח שהמעבר קורה רק אם ה־Fragment מחובר
            viewLifecycleOwner.lifecycleScope.launch {
                delay(600) // לתת ל־Logo להופיע רגע
                if (isAdded && view != null) {
                    findNavController().navigate(action)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

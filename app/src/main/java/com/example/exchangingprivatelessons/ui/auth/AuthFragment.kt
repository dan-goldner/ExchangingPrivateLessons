package com.example.exchangingprivatelessons.ui.auth

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.exchangingprivatelessons.R
import com.example.exchangingprivatelessons.databinding.FragmentAuthBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthFragment : Fragment(R.layout.fragment_auth) {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!
    private val vm: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentAuthBinding.bind(view)

        // לחצן התחברות/הרשמה
        binding.authButton.setOnClickListener {
            vm.authenticate(
                name    = binding.nameEditText.text.toString(),
                email   = binding.emailEditText.text.toString(),
                pass    = binding.passwordEditText.text.toString(),
                confirm = binding.confirmPasswordEditText.text.toString()
            )
            requireActivity().currentFocus?.clearFocus()
        }

        // החלפת מצב LOGIN / SIGNUP
        binding.toggleModeText.setOnClickListener { vm.toggleMode() }

        // צפייה בשינויים במצב המסך
        vm.uiState.observe(viewLifecycleOwner) { render(it) }

        // אירועים חד-פעמיים (כמו ניווט)
        vm.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is AuthViewModel.UiEvent.AuthSuccess -> {
                    findNavController().navigate(AuthFragmentDirections.actionAuthToHome())
                    showToast("Login successfully completed!")
                    vm.clearEvent()  // כדי לא לטרגט שוב בעת סיבוב מסך
                }
                null -> Unit
            }
        }
    }

    private fun render(state: AuthViewModel.UiState) = with(binding) {
        authButton.isEnabled = !state.loading

        if (state.loading) {
            authButton.text = ""
            val spinner = ContextCompat.getDrawable(requireContext(), R.drawable.spinner_rotate)
            authButton.setCompoundDrawablesWithIntrinsicBounds(null, null, spinner, null)
            (spinner as? AnimatedVectorDrawable)?.start()
        } else {
            authButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            authButton.text = if (state.mode == AuthViewModel.Mode.SIGNUP) "הירשם" else "התחבר"
        }

        val isSignup = state.mode == AuthViewModel.Mode.SIGNUP
        nameInputLayout.visibility = if (isSignup) View.VISIBLE else View.GONE
        confirmPasswordInputLayout.visibility = if (isSignup) View.VISIBLE else View.GONE
        titleText.text = if (isSignup) "הרשמה" else "התחברות"
        toggleModeText.text = if (isSignup) "כבר רשום? התחבר" else "אין חשבון? הירשם"

        state.error?.let {
            emailInputLayout.error = if (it.contains("email", ignoreCase = true)) it else null
            passwordInputLayout.error = if (it.contains("password", ignoreCase = true)) it else null
            showToast(it)
            vm.clearError()
        }
    }

    private fun showToast(msg: String) =
        android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_LONG).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

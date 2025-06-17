package com.example.exchangingprivatelessons

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.exchangingprivatelessons.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _b: FragmentLoginBinding? = null
    private val b get() = _b!!
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentLoginBinding.bind(view)
        auth = FirebaseAuth.getInstance()

        /* --- כפתור LOGIN --- */
        b.buttonLogin.setOnClickListener {
            val email     = b.editTextEmail.text.toString().trim()
            val password  = b.editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(),
                    "נא למלא אימייל וסיסמה", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. ניסיון התחברות
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // התחברות הצליחה → ממשיכים ל-Home ללא בדיקות נוספות
                        findNavController()
                            .navigate(R.id.action_loginFragment_to_takeLessonsFragment)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            task.exception?.localizedMessage ?: "שגיאת התחברות",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        /* --- מעבר למסך הרשמה --- */
        b.textViewGoToSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}

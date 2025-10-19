package com.example.project_map.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_map.R
import com.example.project_map.data.HashingUtils
import com.example.project_map.data.UserDatabase
import com.example.project_map.data.UserData
import com.example.project_map.ui.admin.AdminActivity

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvRegister = view.findViewById<TextView>(R.id.tvRegister)

        // Auto-fill for easy user testing
        etEmail.setText("admin@gmail.com")
        etPassword.setText("Admin123_")

        btnLogin.setOnClickListener {
            val emailInput = etEmail.text.toString().trim()
            val passwordInput = etPassword.text.toString().trim()

            if (emailInput.isEmpty() || passwordInput.isEmpty()) {
                Toast.makeText(requireContext(), "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            // 1. Check if the login attempt is for the admin
            if (emailInput.equals("admin@gmail.com", ignoreCase = true)) {
                val adminUser = UserDatabase.allUsers.find { it.isAdmin }
                if (adminUser != null && HashingUtils.hashPassword(passwordInput) == adminUser.pass) {
                    // Admin login is successful
                    saveUserDataAndNavigate(adminUser)
                } else {
                    Toast.makeText(requireContext(), "Password admin salah", Toast.LENGTH_SHORT).show()
                }
            } else {
                // 2. If not admin, treat as a regular user and validate input format
                if (isValidUserCredentials(emailInput, passwordInput)) {
                    // Validation passed, log in as the default "Santi Sanjaya" user
                    val defaultUser = UserDatabase.allUsers.find { it.name == "Santi Sanjaya" }
                    if (defaultUser != null) {
                        saveUserDataAndNavigate(defaultUser)
                    } else {
                        Toast.makeText(requireContext(), "Default user 'Santi Sanjaya' not found in database.", Toast.LENGTH_LONG).show()
                    }
                }
                // If validation fails, the isValidUserCredentials function will show a specific Toast
            }
        }

        tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun saveUserDataAndNavigate(loggedInUser: UserData) {
        val sessionPrefs = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        with(sessionPrefs.edit()) {
            putString("USER_ID", loggedInUser.id)
            putString("USER_NAME", loggedInUser.name)
            putString("USER_EMAIL", loggedInUser.email)
            putString("USER_PHONE", loggedInUser.phone)
            putBoolean("IS_ADMIN", loggedInUser.isAdmin)
            putString("USER_STATUS", loggedInUser.status)
            apply()
        }

        if (loggedInUser.isAdmin) {
            Toast.makeText(requireContext(), "Login sebagai Admin berhasil!", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireActivity(), AdminActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        } else {
            Toast.makeText(requireContext(), "Login sebagai User berhasil!", Toast.LENGTH_SHORT).show()
            if (findNavController().currentDestination?.id == R.id.loginFragment) {
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            }
        }
    }

    /**
     * Validates the format for a standard user's credentials.
     */
    private fun isValidUserCredentials(email: String, pass: String): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), "Format email tidak valid", Toast.LENGTH_SHORT).show()
            return false
        }
        if (pass.length < 8) {
            Toast.makeText(requireContext(), "Password minimal 8 karakter", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!pass.any { it.isUpperCase() }) {
            Toast.makeText(requireContext(), "Password harus memiliki huruf kapital", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!pass.any { it.isLowerCase() }) {
            Toast.makeText(requireContext(), "Password harus memiliki huruf kecil", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!pass.any { it.isDigit() }) {
            Toast.makeText(requireContext(), "Password harus memiliki angka", Toast.LENGTH_SHORT).show()
            return false
        }
        if (pass.all { it.isLetterOrDigit() }) {
            Toast.makeText(requireContext(), "Password harus memiliki simbol", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}

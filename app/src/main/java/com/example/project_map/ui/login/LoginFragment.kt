package com.example.project_map.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
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

        // Auto-fill for easy testing
        etEmail.setText("admin@gmail.com")
        etPassword.setText("admin")

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Check for admin credentials
            if (email == "admin@gmail.com" && password == "admin") {
                Toast.makeText(requireContext(), "Login as Admin successful!", Toast.LENGTH_SHORT).show()

                // --- THIS IS THE CRITICAL CHANGE ---
                // Launch the new AdminActivity instead of navigating to a fragment
                val intent = Intent(requireActivity(), AdminActivity::class.java)
                startActivity(intent)

                // Finish MainActivity so the user cannot press "back" to get to the login screen
                requireActivity().finish()
                // ------------------------------------

            } else {
                // Here you would put your logic for regular user login
                Toast.makeText(requireContext(), "Login as User successful!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            }
        }

        tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
}

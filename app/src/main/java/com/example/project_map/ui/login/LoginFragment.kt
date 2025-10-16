package com.example.project_map.ui.login

import android.content.Context
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

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            // 🔒 Cek apakah login sebagai admin
            if (email == "admin" && password == "admin123") {
                findNavController().navigate(R.id.action_loginFragment_to_adminDashboardFragment)
                return@setOnClickListener
            }

            // 📦 Ambil data user dari SharedPreferences
            val sharedPreferences = requireContext()
                .getSharedPreferences("UserData", Context.MODE_PRIVATE)
            val savedEmail = sharedPreferences.getString("EMAIL", "")
            val savedPassword = sharedPreferences.getString("PASSWORD", "")

            // 👥 Cek apakah login sebagai user biasa
            if (email == savedEmail && password == savedPassword) {
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            } else {
                Toast.makeText(requireContext(), "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }

        tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
}

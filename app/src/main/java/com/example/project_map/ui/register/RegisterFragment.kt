package com.example.project_map.ui.register

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_map.R

class RegisterFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)

        val etName = view.findViewById<EditText>(R.id.etName)
        val etPhone = view.findViewById<EditText>(R.id.etPhone)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = view.findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val cbTerms = view.findViewById<CheckBox>(R.id.cbTerms)
        val tvLoginRedirect = view.findViewById<TextView>(R.id.tvLoginRedirect)

        btnRegister.setOnClickListener {
            val name = etName.text.toString()
            val phone = etPhone.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "All fields must be filled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!cbTerms.isChecked) {
                Toast.makeText(requireContext(), "You must accept the Terms and Conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val editor = sharedPreferences.edit()
            editor.putString("NAME", name)
            editor.putString("PHONE", phone)
            editor.putString("EMAIL", email)
            editor.putString("PASSWORD", password)
            editor.apply()

            Toast.makeText(requireContext(), "Register successful!", Toast.LENGTH_SHORT).show()

            // Navigate back to the login fragment
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        tvLoginRedirect.setOnClickListener {
            // Navigate back to the login fragment
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }
}
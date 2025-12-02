package com.example.project_map.ui.login

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvRegister = view.findViewById<TextView>(R.id.tvRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                // Safe check for context here just in case
                context?.let { ctx ->
                    Toast.makeText(ctx, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    // 1. SAFETY CHECK: If fragment is dead, stop here.
                    if (!isAdded) return@addOnCompleteListener

                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid

                        if (userId != null) {
                            db.collection("users").document(userId).get()
                                .addOnSuccessListener { document ->
                                    // 2. SAFETY CHECK: Again, check if fragment is still alive after DB call
                                    if (!isAdded) return@addOnSuccessListener

                                    if (document.exists()) {
                                        val isAdmin = document.getBoolean("admin") ?: false

                                        if (isAdmin) {
                                            // 3. Use requireContext() which is safe now because we checked isAdded
                                            Toast.makeText(requireContext(), "Welcome Admin", Toast.LENGTH_SHORT).show()
                                            val intent = Intent(requireActivity(), AdminActivity::class.java)
                                            startActivity(intent)
                                            requireActivity().finish()
                                        } else {
                                            Toast.makeText(requireContext(), "Welcome", Toast.LENGTH_SHORT).show()
                                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                                        }
                                    } else {
                                        Toast.makeText(requireContext(), "User data not found in database", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener {
                                    if (isAdded) {
                                        Toast.makeText(requireContext(), "Failed to get user role", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    } else {
                        // Safe context check
                        if (isAdded) {
                            Toast.makeText(requireContext(), "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }

        tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
}
package com.example.project_map.ui.splash

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_map.R
import com.example.project_map.ui.admin.AdminActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rootLayout = view.findViewById<View>(R.id.splashRoot)
        rootLayout.setOnClickListener {
            checkUserSession()
        }
    }

    private fun checkUserSession() {
        // 1. SAFETY CHECK: Only run if we are still on the Splash Screen
        if (findNavController().currentDestination?.id != R.id.splashFragment) {
            return
        }

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    // 2. SAFETY CHECK: Ensure we are still attached and on Splash
                    if (!isAdded || findNavController().currentDestination?.id != R.id.splashFragment) return@addOnSuccessListener

                    if (document.exists()) {
                        val isAdmin = document.getBoolean("admin") ?: false
                        if (isAdmin) {
                            Toast.makeText(context, "Welcome Admin", Toast.LENGTH_SHORT).show()
                            val intent = Intent(requireActivity(), AdminActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        } else {
                            // Navigate to Home
                            findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                        }
                    } else {
                        // User deleted? Go to Login
                        findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
                    }
                }
                .addOnFailureListener {
                    // Network error? Go to Login
                    if (isAdded && findNavController().currentDestination?.id == R.id.splashFragment) {
                        findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
                    }
                }
        } else {
            // Not logged in -> Go to Login
            findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
        }
    }
}
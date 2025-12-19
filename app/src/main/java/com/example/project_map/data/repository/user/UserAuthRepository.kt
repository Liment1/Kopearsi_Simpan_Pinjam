package com.example.project_map.data.repository.user

import com.example.project_map.data.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserAuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Reusing the Result class from Login, adding a generic Success
    sealed class AuthResult {
        object Success : AuthResult()
        data class Error(val message: String) : AuthResult()
    }

    suspend fun registerUser(name: String, phone: String, email: String, pass: String): AuthResult {
        return try {
            // 1. Create User in Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // 2. Generate Custom ID (Business Logic)
                val customId = "AGT" + System.currentTimeMillis().toString().takeLast(4)

                val newUser = UserData(
                    memberCode = customId,
                    name = name,
                    phone = phone,
                    email = email,
                    // pass = "", // Never save raw passwords in DB
                    admin = false,
                    status = "Calon Anggota"
                )

                // 3. Save to Firestore
                db.collection("users").document(firebaseUser.uid).set(newUser).await()

                AuthResult.Success
            } else {
                AuthResult.Error("Registration failed: User ID missing")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Unknown registration error")
        }
    }

    // Define a result sealed class to handle different login outcomes
    sealed class LoginResult {
        object SuccessUser : LoginResult()
        object SuccessAdmin : LoginResult()
        data class Error(val message: String) : LoginResult()
    }

    suspend fun login(email: String, pass: String): LoginResult {
        return try {
            // 1. Attempt Firebase Auth Login
            auth.signInWithEmailAndPassword(email, pass).await()

            val userId = auth.currentUser?.uid
            if (userId != null) {
                // 2. Fetch User Role from Firestore
                val document = db.collection("users").document(userId).get().await()

                if (document.exists()) {
                    val isAdmin = document.getBoolean("admin") ?: false
                    if (isAdmin) LoginResult.SuccessAdmin else LoginResult.SuccessUser
                } else {
                    LoginResult.Error("User data not found in database")
                }
            } else {
                LoginResult.Error("Authentication failed")
            }
        } catch (e: Exception) {
            LoginResult.Error(e.message ?: "Unknown login error")
        }
    }

    suspend fun checkSession(): LoginResult {
        val user = auth.currentUser
        // 1. If no user is cached locally, force them to login
        if (user == null) {
            return LoginResult.Error("No active session")
        }

        return try {
            // 2. User exists, check their role in Firestore
            val document = db.collection("users").document(user.uid).get().await()

            if (document.exists()) {
                val isAdmin = document.getBoolean("admin") ?: false
                if (isAdmin) LoginResult.SuccessAdmin else LoginResult.SuccessUser
            } else {
                // User is in Auth but not in DB (rare edge case)
                LoginResult.Error("User data missing")
            }
        } catch (e: Exception) {
            LoginResult.Error(e.message ?: "Connection error")
        }
    }


    fun logout() {
        auth.signOut()
    }

}
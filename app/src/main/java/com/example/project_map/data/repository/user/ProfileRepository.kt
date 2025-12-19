package com.example.project_map.data.repository.user

import android.net.Uri
import com.example.project_map.data.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage // Import Firebase Storage
import kotlinx.coroutines.tasks.await

class ProfileRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance() // Initialize Storage

    // 1. Fetch User Data
    suspend fun getUserProfile(userId: String): UserData {
        return try {
            val document = db.collection("users").document(userId).get().await()
            if (document.exists()) {
                UserData(
                    id = userId,
                    name = document.getString("name") ?: "No Name",
                    email = document.getString("email") ?: "",
                    status = document.getString("status") ?: "Tidak Aktif",
                    memberCode = document.getString("memberCode") ?: "",
                    phone = document.getString("phone") ?: "",
                    admin = document.getBoolean("admin") ?: false,
                    avatarUrl = document.getString("avatarUrl") ?: ""
                )
            } else {
                UserData(id = userId)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    // 2. Upload Image to Firebase Storage (Replaces Cloudinary)
    suspend fun uploadImageToFirebase(userId: String, imageUri: Uri): String {
        return try {
            // Directory: avatars/USER_ID.jpg
            // Using userId as filename allows overwriting old avatar automatically
            val ref = storage.reference.child("avatars/$userId.jpg")

            ref.putFile(imageUri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw Exception("Avatar upload failed: ${e.message}")
        }
    }

    // 3. Update Avatar URL in Firestore
    suspend fun updateAvatarUrl(userId: String, url: String) {
        db.collection("users").document(userId)
            .update("avatarUrl", url)
            .await()
    }

    // 4. Update Profile Text Data
    suspend fun updateProfileData(userId: String, name: String, email: String, phone: String) {
        val userUpdates = mapOf(
            "name" to name,
            "email" to email,
            "phone" to phone
        )
        db.collection("users").document(userId)
            .update(userUpdates)
            .await()
    }

    // 5. Logout Function
    fun logout() {
        auth.signOut()
    }
}
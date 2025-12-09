package com.example.project_map.data.repository

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.project_map.data.UserData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ProfileRepository {

    private val db = FirebaseFirestore.getInstance()

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

    // 2. Upload Image to Cloudinary
    suspend fun uploadImageToCloudinary(userId: String, imageUri: Uri): String {
        return suspendCoroutine { continuation ->
            MediaManager.get().upload(imageUri)
                .unsigned("userss") // Make sure this matches your Cloudinary preset exactly
                .option("public_id", "user_${userId}_${System.currentTimeMillis()}")
                .option("folder", "avatars")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val secureUrl = resultData["secure_url"].toString()
                        continuation.resume(secureUrl)
                    }
                    override fun onError(requestId: String, error: ErrorInfo) {
                        continuation.resumeWithException(Exception(error.description))
                    }
                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                })
                .dispatch()
        }
    }

    // 3. Update Avatar URL in Firestore
    suspend fun updateAvatarUrl(userId: String, url: String) {
        db.collection("users").document(userId)
            .update("avatarUrl", url)
            .await()
    }

    // 4. Update Profile Text Data (NEW - Added for MVVM)
    suspend fun updateProfileData(userId: String, name: String, email: String, phone: String) {
        val userUpdates = mapOf(
            "name" to name,
            "email" to email,
            "phone" to phone
        )
        // .await() makes this a suspending function, keeping it off the main thread
        db.collection("users").document(userId)
            .update(userUpdates)
            .await()
    }
}
package com.example.project_map.data.repository

import android.net.Uri
//import com.google.firebase.storage.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class StorageRepository {
    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadImage(imageUri: Uri, folderPath: String): Result<String> {
        return try {
            // Create a unique filename
            val fileName = "${System.currentTimeMillis()}.jpg"
            val storageRef = storage.reference.child("$folderPath/$fileName")

            // 1. Upload the file
            storageRef.putFile(imageUri).await()

            // 2. Get the public Download URL
            val downloadUrl = storageRef.downloadUrl.await()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
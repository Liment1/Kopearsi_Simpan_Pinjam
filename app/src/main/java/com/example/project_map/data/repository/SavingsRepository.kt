package com.example.project_map.data.repository

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.project_map.data.Transaction
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import android.content.Context

class SavingsRepository {

    private val db = FirebaseFirestore.getInstance()

    // 1. Upload Image to Cloudinary (Adapted from ProfileRepository)
    suspend fun uploadProofToCloudinary(userId: String, imageUri: Uri): String {
        return suspendCoroutine { continuation ->
            MediaManager.get().upload(imageUri)
                .unsigned("savings_preset") // Using the same preset as ProfileRepository
                .option("public_id", "savings_${userId}_${System.currentTimeMillis()}")
                .option("folder", "savings_proofs") // Changed folder to keep it organized
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

    // 2. Save Transaction to Firestore (Moved from SavingsFragment)
    suspend fun saveTransaction(userId: String, amount: Double, type: String, imageUrl: String?) {
        val batch = db.batch()
        val userRef = db.collection("users").document(userId)
        val newTransRef = userRef.collection("savings").document()

        val transaction = Transaction(
            id = newTransRef.id,
            date = Date(),
            type = type,
            amount = amount,
            description = "Setoran $type",
            imageUri = imageUrl // This is now the Cloudinary URL
        )

        batch.set(newTransRef, transaction)

        // Update Totals
        batch.update(userRef, "totalSimpanan", FieldValue.increment(amount))

        val fieldToUpdate = when (type) {
            "Simpanan Pokok" -> "simpananPokok"
            "Simpanan Wajib" -> "simpananWajib"
            "Simpanan Sukarela" -> "simpananSukarela"
            else -> "simpananSukarela"
        }
        batch.update(userRef, fieldToUpdate, FieldValue.increment(amount))

        batch.commit().await()
    }

}
package com.example.project_map.data.repository.user

import android.net.Uri
import com.example.project_map.data.model.Savings
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage // Import Firebase Storage
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

class SavingsRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance() // Initialize Storage

    /**
     * 1. Listen to User Balances (Realtime)
     */
    fun getUserBalanceStream(userId: String, onUpdate: (Map<String, Double>) -> Unit): ListenerRegistration {
        return db.collection("users").document(userId)
            .addSnapshotListener { document, e ->
                if (e != null || document == null || !document.exists()) return@addSnapshotListener

                val data = mapOf(
                    "total" to (document.getDouble("totalSimpanan") ?: 0.0),
                    "pokok" to (document.getDouble("simpananPokok") ?: 0.0),
                    "wajib" to (document.getDouble("simpananWajib") ?: 0.0),
                    "sukarela" to (document.getDouble("simpananSukarela") ?: 0.0)
                )
                onUpdate(data)
            }
    }

    /**
     * 2. Listen to Transaction History (Realtime)
     */
    fun getTransactionStream(userId: String, onUpdate: (List<Savings>) -> Unit): ListenerRegistration {
        return db.collection("users").document(userId).collection("savings")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener
                val list = snapshots.toObjects(Savings::class.java)
                onUpdate(list)
            }
    }

    /**
     * 3. Upload Image & Save Transaction
     */
    suspend fun uploadAndSaveTransaction(userId: String, amount: Double, type: String, localUri: Uri) {
        // Step A: Upload Image to Firebase Storage
        // Directory: savings/{userId}/{randomId}.jpg
        val imageUrl = uploadImageToFirebase(userId, localUri)

        // Step B: Save Data & Update Balance atomically
        saveTransactionToFirestore(userId, amount, type, imageUrl)
    }

    // --- REPLACED CLOUDINARY WITH FIREBASE STORAGE ---
    private suspend fun uploadImageToFirebase(userId: String, fileUri: Uri): String {
        return try {
            // Create a unique filename
            val filename = "${UUID.randomUUID()}.jpg"
            // Reference: savings/USER_ID/FILENAME
            val ref = storage.reference.child("savings/$userId/$filename")

            // 1. Upload the file
            ref.putFile(fileUri).await()

            // 2. Get the download URL
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw Exception("Upload failed: ${e.message}")
        }
    }

    /**
     * 4. Atomic Transaction
     */
    private suspend fun saveTransactionToFirestore(userId: String, amount: Double, type: String, imageUrl: String?) {
        val userRef = db.collection("users").document(userId)
        val newTxRef = userRef.collection("savings").document()

        val newSavings = Savings(
            id = newTxRef.id,
            date = Date(),
            type = type,
            amount = amount,
            description = "Setoran via Aplikasi",
            imageUri = imageUrl
        )

        db.runTransaction { transaction ->
            // 1. Create the transaction record
            transaction.set(newTxRef, newSavings)

            // 2. Determine field
            val fieldToUpdate = when (type) {
                "Simpanan Pokok" -> "simpananPokok"
                "Simpanan Wajib" -> "simpananWajib"
                "Simpanan Sukarela" -> "simpananSukarela"
                else -> "simpananSukarela"
            }

            // 3. Update specific balance
            transaction.update(userRef, fieldToUpdate, FieldValue.increment(amount))

            // 4. Update TOTAL balance
            transaction.update(userRef, "totalSimpanan", FieldValue.increment(amount))

        }.await()
    }
}
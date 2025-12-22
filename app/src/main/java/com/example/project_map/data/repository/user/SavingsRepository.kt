package com.example.project_map.data.repository.user

import android.net.Uri
import android.util.Log
import com.example.project_map.data.model.Savings
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

data class UserFinancialData(
    val status: String,
    val balances: Map<String, Double>
)

class SavingsRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun getUserStream(userId: String, onUpdate: (UserFinancialData) -> Unit): ListenerRegistration {
        return db.collection("users").document(userId)
            .addSnapshotListener { document, e ->
                if (e != null) {
                    Log.e("SavingsRepo", "Savings listen failed", e)
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val status = document.getString("status") ?: "Calon Anggota"
                    val balances = mapOf(
                        "total" to getSafeDouble(document, "totalSimpanan"),
                        "pokok" to getSafeDouble(document, "simpananPokok"),
                        "wajib" to getSafeDouble(document, "simpananWajib"),
                        "sukarela" to getSafeDouble(document, "simpananSukarela")
                    )
                    onUpdate(UserFinancialData(status, balances))
                } else {
                    onUpdate(UserFinancialData("Calon Anggota",
                        mapOf("total" to 0.0, "pokok" to 0.0, "wajib" to 0.0, "sukarela" to 0.0)))
                }
            }
    }

    fun getSavingsHistory(userId: String, onUpdate: (List<Savings>) -> Unit): ListenerRegistration {
        return db.collection("users").document(userId).collection("savings")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                val list = snapshots?.toObjects(Savings::class.java) ?: emptyList()
                onUpdate(list)
            }
    }

    // --- DIRECT DEPOSIT (Immediate Balance Increase) ---
    suspend fun requestDeposit(userId: String, userName: String, amount: Double, fileUri: Uri): Result<String> {
        return try {
            // 1. Upload Image Proof
            val filename = "${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child("deposit_proofs/$userId/$filename")
            ref.putFile(fileUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()

            // 2. Prepare References
            val userRef = db.collection("users").document(userId)
            val newSavingsRef = userRef.collection("savings").document() // New ID

            // 3. Run Transaction (Update Balance + Create History)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)

                // Get current balances
                val currentSukarela = snapshot.getDouble("simpananSukarela") ?: 0.0
                val currentTotal = snapshot.getDouble("totalSimpanan") ?: 0.0

                // Calculate new balances
                val newSukarela = currentSukarela + amount
                val newTotal = currentTotal + amount

                // A. Update User Balance
                transaction.update(userRef, "simpananSukarela", newSukarela)
                transaction.update(userRef, "totalSimpanan", newTotal)

                // B. Create History Entry
                val historyData = hashMapOf(
                    "id" to newSavingsRef.id,
                    "userId" to userId,
                    "userName" to userName,
                    "amount" to amount,
                    "type" to "Simpanan Sukarela",
                    "status" to "Selesai",
                    "proofUrl" to downloadUrl,
                    "date" to Date(),
                    "timestamp" to FieldValue.serverTimestamp()
                )
                transaction.set(newSavingsRef, historyData)
            }.await()

            Result.success("Deposit berhasil. Saldo telah bertambah.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- DIRECT WITHDRAWAL (Immediate Balance Decrease) ---
    suspend fun requestWithdrawal(userId: String, userName: String, amount: Double, bankName: String, accountNumber: String): Result<String> {
        return try {
            val userRef = db.collection("users").document(userId)
            val newSavingsRef = userRef.collection("savings").document()

            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)

                val currentSukarela = snapshot.getDouble("simpananSukarela") ?: 0.0
                val currentTotal = snapshot.getDouble("totalSimpanan") ?: 0.0

                // Check sufficient funds (Optional safety)
                if (currentSukarela < amount) {
                    throw Exception("Saldo Sukarela tidak mencukupi.")
                }

                val newSukarela = currentSukarela - amount
                val newTotal = currentTotal - amount

                // A. Update User Balance
                transaction.update(userRef, "simpananSukarela", newSukarela)
                transaction.update(userRef, "totalSimpanan", newTotal)

                // B. Create History Entry
                val historyData = hashMapOf(
                    "id" to newSavingsRef.id,
                    "userId" to userId,
                    "userName" to userName,
                    "amount" to amount,
                    "bankName" to bankName,
                    "accountNumber" to accountNumber,
                    "type" to "Penarikan",
                    "status" to "Selesai", // Immediately Completed
                    "date" to Date(),
                    "timestamp" to FieldValue.serverTimestamp()
                )
                transaction.set(newSavingsRef, historyData)
            }.await()

            Result.success("Penarikan berhasil. Saldo telah berkurang.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getSafeDouble(doc: DocumentSnapshot, field: String): Double {
        val value = doc.get(field)
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }
}
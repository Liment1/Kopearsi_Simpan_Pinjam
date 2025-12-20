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

// 1. Helper data class to transport both Status and Balances
data class UserFinancialData(
    val status: String,
    val balances: Map<String, Double>
)

class SavingsRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // 2. Renamed to getUserStream and updated return type
    fun getUserStream(userId: String, onUpdate: (UserFinancialData) -> Unit): ListenerRegistration {
        return db.collection("users").document(userId)
            .addSnapshotListener { document, e ->
                if (e != null) {
                    Log.e("SavingsRepo", "Savings listen failed", e)
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    // Fetch Status (default to "Calon Anggota" if missing)
                    val status = document.getString("status") ?: "Calon Anggota"

                    val balances = mapOf(
                        "total" to getSafeDouble(document, "totalSimpanan"),
                        "pokok" to getSafeDouble(document, "simpananPokok"),
                        "wajib" to getSafeDouble(document, "simpananWajib"),
                        "sukarela" to getSafeDouble(document, "simpananSukarela")
                    )

                    onUpdate(UserFinancialData(status, balances))
                } else {
                    // Default for new/missing users
                    onUpdate(UserFinancialData(
                        "Calon Anggota",
                        mapOf("total" to 0.0, "pokok" to 0.0, "wajib" to 0.0, "sukarela" to 0.0)
                    ))
                }
            }
    }

    fun getSavingsHistory(userId: String, onUpdate: (List<Savings>) -> Unit): ListenerRegistration {
        return db.collection("users").document(userId).collection("savings")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("SavingsRepo", "Savings listen failed", e)
                    return@addSnapshotListener
                }
                val list = snapshots?.toObjects(Savings::class.java) ?: emptyList()
                onUpdate(list)
            }
    }

    suspend fun requestDeposit(userId: String, userName: String, amount: Double): Result<String> {
        return try {
            val request = hashMapOf(
                "userId" to userId,
                "userName" to userName,
                "amount" to amount,
                "type" to "Deposit",
                "status" to "Pending",
                "date" to Date(),
                "timestamp" to FieldValue.serverTimestamp()
            )
            db.collection("deposit_requests").add(request).await()
            Result.success("Deposit request submitted")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun requestWithdrawal(userId: String, userName: String, amount: Double, bankName: String, accountNumber: String): Result<String> {
        return try {
            val request = hashMapOf(
                "userId" to userId,
                "userName" to userName,
                "amount" to amount,
                "bankName" to bankName,
                "accountNumber" to accountNumber,
                "type" to "Withdrawal",
                "status" to "Pending",
                "date" to Date(),
                "timestamp" to FieldValue.serverTimestamp()
            )
            db.collection("withdrawal_requests").add(request).await()
            Result.success("Withdrawal request submitted")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- HELPER FUNCTIONS ---

    private fun getSafeDouble(doc: DocumentSnapshot, field: String): Double {
        val value = doc.get(field)
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }
}
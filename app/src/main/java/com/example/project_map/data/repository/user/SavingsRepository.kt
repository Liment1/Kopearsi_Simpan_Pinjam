package com.example.project_map.data.repository.user

import com.example.project_map.data.model.Savings
import com.example.project_map.data.model.Savings
import com.example.project_map.data.model.WithdrawalRequest // Reuse or create DepositRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import kotlin.jvm.java

class SavingsRepository {
    private val db = FirebaseFirestore.getInstance()

    // 1. Get Savings History
    fun getSavingsHistory(userId: String): Flow<List<Savings>> = callbackFlow {
        val listener = db.collection("users").document(userId)
            .collection("savings")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = value?.toObjects(Savings::class.java) ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // 2. Request Deposit (Simpanan Sukarela)
    suspend fun requestDeposit(userId: String, userName: String, amount: Double): Result<Unit> {
        return try {
            val ref = db.collection("users").document(userId).collection("deposits").document()

            // We create a temporary object. You can create a specific DepositRequest model if preferred.
            val request = hashMapOf(
                "id" to ref.id,
                "userId" to userId,
                "userName" to userName,
                "amount" to amount,
                "type" to "Simpanan Sukarela",
                "status" to "Pending",
                "date" to Date()
            )

            ref.set(request).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. Request Withdrawal (Existing - Ensure it points to subcollection)
    // ... imports

    // Update the function signature to accept bankName
    suspend fun requestWithdrawal(userId: String, userName: String, amount: Double, bankName: String, accountNumber: String): Result<Unit> {
        return try {
            val userRef = db.collection("users").document(userId)
            val currentBalance = userRef.get().await().getDouble("totalSimpanan") ?: 0.0

            if (currentBalance < amount) {
                return Result.failure(Exception("Saldo tidak mencukupi."))
            }

            // Save to subcollection "withdrawals"
            val ref = userRef.collection("withdrawals").document()

            val request = WithdrawalRequest(
                id = ref.id,
                userId = userId,
                userName = userName,
                amount = amount,
                bankName = bankName, // Save it
                accountNumber = accountNumber,
                status = "Pending",
                requestDate = Date()
            )

            ref.set(request).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // ... (Keep existing getUserBalanceStream) ...
    fun getUserBalanceStream(userId: String, onUpdate: (Map<String, Double>) -> Unit) {
        db.collection("users").document(userId)
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
}
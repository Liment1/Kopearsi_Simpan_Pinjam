package com.example.project_map.data.repository.admin

import android.R.attr.description
import android.R.attr.type
import com.example.project_map.data.model.FinancialTransaction
import com.example.project_map.data.model.Savings
import com.example.project_map.data.model.WithdrawalRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class AdminWithdrawalRepository {
    private val db = FirebaseFirestore.getInstance()

    // 1. Stream Pending Requests
    fun getPendingWithdrawals(): Flow<List<WithdrawalRequest>> = callbackFlow {
        val listener = db.collection("withdrawal_requests")
            .whereEqualTo("status", "Pending")
            .orderBy("requestDate", Query.Direction.ASCENDING) // Oldest first
            .addSnapshotListener { value, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = value?.toObjects(WithdrawalRequest::class.java) ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // 2. Approve Withdrawal (Atomic Transaction)
    suspend fun approveWithdrawal(request: WithdrawalRequest): Result<Unit> {
        return try {
            val userRef = db.collection("users").document(request.userId)
            val requestRef = userRef.collection("withdrawals").document(request.id) // Subcollection path
            val historyRef = userRef.collection("savings").document()

            db.runTransaction { transaction ->
                val userDoc = transaction.get(userRef)
                val currentBalance = userDoc.getDouble("totalSimpanan") ?: 0.0

                if (currentBalance < request.amount) {
                    throw Exception("Saldo user tidak mencukupi.")
                }

                // 1. Deduct Balance
                transaction.update(userRef, "totalSimpanan", FieldValue.increment(-request.amount))

                // 2. Create Transaction History
                val transLog = FinancialTransaction(
                    id = historyRef.id,
                    amount = request.amount,
                    date = Date(),
                    type = "Penarikan Dana",
                    category = "Penarikan",
                    // Simple description without bank name
                    description = "Penarikan ke Rekening ${request.accountNumber}"
                )
                transaction.set(historyRef, transLog)

                // 3. Update Status
                transaction.update(requestRef, "status", "Approved")
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. Reject Withdrawal
    suspend fun rejectWithdrawal(requestId: String): Result<Unit> {
        return try {
            db.collection("withdrawal_requests").document(requestId)
                .update("status", "Rejected")
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
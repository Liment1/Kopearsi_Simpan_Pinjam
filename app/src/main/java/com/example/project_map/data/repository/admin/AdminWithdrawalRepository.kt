package com.example.project_map.data.repository.admin

import com.example.project_map.data.model.WithdrawalRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class AdminWithdrawalRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getPendingWithdrawals(): Flow<List<WithdrawalRequest>> = callbackFlow {
        val listener = db.collection("withdrawal_requests")
            .whereEqualTo("status", "Pending")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val requests = value?.toObjects(WithdrawalRequest::class.java) ?: emptyList()

                value?.documents?.forEachIndexed { index, doc ->
                    requests[index].id = doc.id
                }

                CoroutineScope(Dispatchers.IO).launch {
                    requests.forEach { req ->
                        try {
                            val userDoc = db.collection("users").document(req.userId).get().await()
                            req.userName = userDoc.getString("name") ?: "Tanpa Nama"
                            req.userAvatarUrl = userDoc.getString("avatarUrl") ?: ""
                        } catch (e: Exception) {
                            req.userName = "User Error"
                        }
                    }
                    trySend(requests)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun approveWithdrawal(req: WithdrawalRequest): Result<Unit> {
        return try {
            val batch = db.batch()

            // Move to History
            val historyRef = db.collection("users").document(req.userId).collection("savings").document()
            val historyData = hashMapOf(
                "userId" to req.userId,
                "userName" to req.userName,
                "amount" to req.amount,
                "type" to "Penarikan Tunai",
                "status" to "Selesai",
                "date" to Date(),
                "description" to "Transfer ke ${req.bankName} (${req.accountNumber})"
            )
            batch.set(historyRef, historyData)

            // Delete Request
            batch.delete(db.collection("withdrawal_requests").document(req.id))

            // Notify
            val notifRef = db.collection("notifications").document()
            batch.set(notifRef, hashMapOf(
                "userId" to req.userId,
                "title" to "Penarikan Disetujui",
                "message" to "Dana Rp ${req.amount.toInt()} telah ditransfer.",
                "timestamp" to System.currentTimeMillis()
            ))

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun rejectWithdrawal(req: WithdrawalRequest, reason: String): Result<Unit> {
        return try {
            val userRef = db.collection("users").document(req.userId)
            val requestRef = db.collection("withdrawal_requests").document(req.id)
            val historyRef = db.collection("users").document(req.userId).collection("savings").document()

            db.runTransaction { transaction ->
                transaction.update(userRef, "simpananSukarela", FieldValue.increment(req.amount))
                transaction.update(userRef, "totalSimpanan", FieldValue.increment(req.amount))

                // Create History (Rejected)
                transaction.set(historyRef, hashMapOf(
                    "userId" to req.userId,
                    "userName" to req.userName,
                    "amount" to req.amount,
                    "type" to "Penarikan Tunai",
                    "status" to "Ditolak",
                    "date" to Date(),
                    "description" to "Ditolak: $reason"
                ))

                // Delete Request
                transaction.delete(requestRef)
            }.await()

            // Notify (Outside transaction)
            db.collection("notifications").add(hashMapOf(
                "userId" to req.userId,
                "title" to "Penarikan Ditolak",
                "message" to "Dana dikembalikan. Alasan: $reason",
                "timestamp" to System.currentTimeMillis()
            ))

            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}
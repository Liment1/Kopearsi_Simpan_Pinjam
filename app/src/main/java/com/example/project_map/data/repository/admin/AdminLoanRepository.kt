package com.example.project_map.data.repository.admin

import com.example.project_map.data.model.Loan
import com.example.project_map.data.model.UserData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AdminLoanRepository {

    private val db = FirebaseFirestore.getInstance()

    // ... (getAllLoansStream stays the same) ...
    fun getAllLoansStream(): Flow<List<Loan>> = callbackFlow {
        val listener = db.collectionGroup("loans")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val loans = value?.documents?.mapNotNull { doc ->
                    val loan = doc.toObject(Loan::class.java)
                    loan?.apply { id = doc.id }
                } ?: emptyList()
                trySend(loans)
            }
        awaitClose { listener.remove() }
    }

    // --- HELPER: Get User Detail (Was Missing) ---
    suspend fun getUserDetail(userId: String): Result<UserData> {
        return try {
            val snapshot = db.collection("users").document(userId).get().await()
            val user = snapshot.toObject(UserData::class.java)
            if (user != null) Result.success(user) else Result.failure(Exception("User not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- HELPER: Get User Name (Required by AdminLoanViewModel) ---
    suspend fun getUserName(userId: String): String {
        return try {
            if (userId.isEmpty()) return "Unknown"
            val snapshot = db.collection("users").document(userId).get().await()
            snapshot.getString("name") ?: "Tanpa Nama"
        } catch (e: Exception) { "Unknown User" }
    }

    // --- UPDATE LOGIC ---
    suspend fun updateLoanStatus(loanId: String, userId: String, status: String): Result<Unit> {
        return try {
            val batch = db.batch()
            // Path: users/{userId}/loans/{loanId}
            val loanRef = db.collection("users").document(userId).collection("loans").document(loanId)

            batch.update(loanRef, "status", status)

            // Notify
            if (userId.isNotEmpty()) {
                val notifRef = db.collection("notifications").document()
                val notif = hashMapOf(
                    "userId" to userId,
                    "title" to "Status Pinjaman",
                    "message" to "Pengajuan pinjaman Anda telah $status.",
                    "isRead" to false,
                    "timestamp" to System.currentTimeMillis()
                )
                batch.set(notifRef, notif)
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // FIX: Pass userId to these functions
    suspend fun approveLoan(loanId: String, userId: String) = updateLoanStatus(loanId, userId, "Disetujui")

    suspend fun rejectLoan(loanId: String, userId: String, reason: String): Result<Unit> {
        return try {
            val loanRef = db.collection("users").document(userId).collection("loans").document(loanId)

            // FIX: Transaction now updates Status AND creates Notification
            db.runTransaction { transaction ->
                // 1. Update Loan Status
                transaction.update(loanRef, mapOf("status" to "Ditolak", "alasanPenolakan" to reason))

                // 2. Create Notification Document
                val notifRef = db.collection("notifications").document()
                val notif = hashMapOf(
                    "userId" to userId,
                    "title" to "Pinjaman Ditolak",
                    "message" to "Maaf, pengajuan Anda ditolak. Alasan: $reason",
                    "type" to "loan_update",
                    "loanId" to loanId,
                    "isRead" to false,
                    "timestamp" to System.currentTimeMillis()
                )
                transaction.set(notifRef, notif)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getLoanDetail(loanId: String, userId: String): Result<Loan> {
        return try {
            val doc = db.collection("users").document(userId).collection("loans").document(loanId).get().await()
            val loan = doc.toObject(Loan::class.java)?.apply { id = doc.id }
            if (loan != null) Result.success(loan) else Result.failure(Exception("Loan not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
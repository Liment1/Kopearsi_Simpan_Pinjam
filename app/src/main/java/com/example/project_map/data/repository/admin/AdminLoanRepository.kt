package com.example.project_map.data.repository.admin

import com.example.project_map.data.model.Loan
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AdminLoanRepository {

    private val db = FirebaseFirestore.getInstance()

    // CHANGED: Use collectionGroup because loans are inside users/{uid}/loans
    private val loansQuery = db.collectionGroup("loans")

    fun getAllLoansStream(): Flow<List<Loan>> = callbackFlow {
        val listener = loansQuery
            .orderBy("tanggalPengajuan", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val loans = snapshot?.documents?.mapNotNull { doc ->
                    val loan = doc.toObject(Loan::class.java)
                    loan?.apply { id = doc.id } // Ensure ID is captured
                } ?: emptyList()

                trySend(loans)
            }

        awaitClose { listener.remove() }
    }

    suspend fun approveLoan(loanId: String): Result<Unit> {
        return try {
            // We need to find the document reference first because it's in a subcollection
            val snapshot = loansQuery.whereEqualTo("id", loanId).get().await()
            if (snapshot.isEmpty) throw Exception("Loan not found")

            val docRef = snapshot.documents.first().reference

            docRef.update(
                mapOf("status" to "Pinjaman Berjalan", "statusDetail" to "Menunggu Pencairan")
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectLoan(loanId: String, reason: String): Result<Unit> {
        return try {
            val snapshot = loansQuery.whereEqualTo("id", loanId).get().await()
            if (snapshot.isEmpty) throw Exception("Loan not found")

            val docRef = snapshot.documents.first().reference

            val updates = mapOf("status" to "Ditolak", "alasanPenolakan" to reason)
            docRef.update(updates).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
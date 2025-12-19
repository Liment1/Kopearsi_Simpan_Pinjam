package com.example.project_map.ui.admin.report

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// 1. Create a simple data model for Transactions if you haven't already
data class Transaction(
    val id: String = "",
    val type: String = "", // "Pemasukan" or "Pengeluaran"
    val category: String = "", // "Simpanan Wajib", "Angsuran", "Pencairan Pinjaman"
    val amount: Double = 0.0,
    val date: java.util.Date? = null,
    val description: String = ""
)

class AdminFinancialRepository {
    private val db = FirebaseFirestore.getInstance()

    // Stream for Cash Flow (All transactions sorted by date)
    fun getCashFlowStream(): Flow<List<Transaction>> = callbackFlow {
        val listener = db.collection("transactions")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull {
                    it.toObject(Transaction::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }
}
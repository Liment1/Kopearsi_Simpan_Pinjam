package com.example.project_map.data.repository.admin

import android.util.Log
import com.example.project_map.data.model.FinancialTransaction
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Date

class AdminFinancialRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getCashFlowStream(): Flow<List<FinancialTransaction>> = callbackFlow {
        val combinedList = mutableListOf<FinancialTransaction>()

        // 1. Listen to SAVINGS (Pemasukan)
        val savingsListener = db.collectionGroup("savings")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AdminRepo", "Savings Error: ${error.message}")
                    return@addSnapshotListener
                }

                val savings = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        FinancialTransaction(
                            id = doc.id,
                            amount = doc.getDouble("amount") ?: 0.0,
                            type = "Pemasukan",
                            category = doc.getString("type") ?: "Simpanan",
                            date = doc.getDate("date"),
                            description = doc.getString("description") ?: ""
                        )
                    } catch (e: Exception) { null }
                } ?: emptyList()

                updateCombinedList(savings, "savings", this::trySend)
            }

        // 2. Listen to LOANS (Pengeluaran - Money leaving the co-op)
        val loansListener = db.collectionGroup("loans")
            .orderBy("tanggalPengajuan", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AdminRepo", "Loans Error: ${error.message}")
                    return@addSnapshotListener
                }

                val loans = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        FinancialTransaction(
                            id = doc.id,
                            amount = doc.getDouble("nominal") ?: 0.0,
                            type = "Pengeluaran", // Loans are money going OUT
                            category = "Pinjaman Anggota",
                            date = doc.getDate("tanggalPengajuan"),
                            description = "Pinjaman: ${doc.getString("tujuan")}"
                        )
                    } catch (e: Exception) { null }
                } ?: emptyList()

                updateCombinedList(loans, "loans", this::trySend)
            }

        awaitClose {
            savingsListener.remove()
            loansListener.remove()
        }
    }

    // Helper to merge lists safely
    private var cachedSavings = listOf<FinancialTransaction>()
    private var cachedLoans = listOf<FinancialTransaction>()

    private fun updateCombinedList(
        newList: List<FinancialTransaction>,
        source: String,
        send: (List<FinancialTransaction>) -> Unit
    ) {
        if (source == "savings") cachedSavings = newList
        if (source == "loans") cachedLoans = newList

        val merged = (cachedSavings + cachedLoans).sortedByDescending { it.date }
        Log.d("AdminRepo", "Merged ${merged.size} transactions")
        send(merged)
    }
}
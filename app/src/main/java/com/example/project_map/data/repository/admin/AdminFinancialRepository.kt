package com.example.project_map.data.repository.admin // Consider moving to data.repository.admin

import com.example.project_map.data.model.FinancialTransaction
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AdminFinancialRepository {
    private val db = FirebaseFirestore.getInstance()

    // Stream for Cash Flow (All transactions sorted by date)
    fun getCashFlowStream(): Flow<List<FinancialTransaction>> = callbackFlow {
        val listener = db.collection("transactions")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        // Using the separate FinancialTransaction class
                        doc.toObject(FinancialTransaction::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null // Skip malformed documents
                    }
                } ?: emptyList()

                trySend(list)
            }

        awaitClose { listener.remove() }
    }
}
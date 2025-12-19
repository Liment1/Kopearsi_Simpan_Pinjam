package com.example.project_map.data.repository.admin

import com.example.project_map.data.model.Installment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AdminInstallmentRepository {

    private val db = FirebaseFirestore.getInstance()

    // Real-time stream of ALL Paid Installments
    fun getAllInstallmentsStream(): Flow<List<Installment>> = callbackFlow {
        val listener = db.collectionGroup("installments")
            .whereEqualTo("status", "Lunas") // Filter for PAID installments
            .orderBy("tanggalBayar", Query.Direction.DESCENDING) // Sort by Payment Date
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) // This will crash if Index is missing (Good, so you can see the link)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Installment::class.java)?.copy(id = doc.id)
                    }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }
}
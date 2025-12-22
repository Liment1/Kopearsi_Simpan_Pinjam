package com.example.project_map.data.repository.admin

import android.util.Log
import com.example.project_map.data.model.Installment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AdminInstallmentRepository {

    private val db = FirebaseFirestore.getInstance()

    fun listenToPaidInstallments(
        onSuccess: (List<Installment>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collectionGroup("installments")
            .whereEqualTo("status", "Lunas") // Only Paid items
            .orderBy("tanggalBayar", Query.Direction.DESCENDING) // Newest first
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("AdminInstallment", "Listen failed.", error)
                    onError(error)
                    return@addSnapshotListener
                }

                if (value == null || value.isEmpty) {
                    onSuccess(emptyList())
                    return@addSnapshotListener
                }

                val rawList = value.documents.mapNotNull { doc ->
                    doc.toObject(Installment::class.java)?.apply {
                        id = doc.id
                    }
                }

                // Hydrate Names & IDs
                CoroutineScope(Dispatchers.IO).launch {
                    val hydratedList = rawList.map { item ->
                        try {
                            val docSnapshot = value.documents.find { it.id == item.id }
                            if (docSnapshot != null) {
                                val installmentRef = docSnapshot.reference
                                val loanRef = installmentRef.parent.parent

                                if (loanRef != null) {
                                    item.loanId = loanRef.id

                                    val loanSnap = loanRef.get().await()
                                    // Get Loan Name/Type if available, or Borrower Name
                                    val loanType = loanSnap.getString("jenisPinjaman") ?: "Pinjaman"
                                    val borrowerName = loanSnap.getString("namaPeminjam")
                                        ?: loanSnap.getString("name")
                                        ?: "Unknown"

                                    // Combine them for display: "Budi - Pinjaman Usaha"
                                    item.peminjamName = "$borrowerName ($loanType)"

                                    val userRef = loanRef.parent.parent
                                    if (userRef != null) {
                                        item.userId = userRef.id
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("Repo", "Error hydrating ${item.id}", e)
                        }
                        item
                    }
                    withContext(Dispatchers.Main) {
                        onSuccess(hydratedList)
                    }
                }
            }
    }
}
package com.example.project_map.data.repository.user

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class UserHomeRepository {
    private val db = FirebaseFirestore.getInstance()

    // 1. Get User Profile Realtime (Name & Balance)
    fun addUserListener(userId: String, onUpdate: (String, Double) -> Unit): ListenerRegistration {
        return db.collection("users").document(userId)
            .addSnapshotListener { document, e ->
                if (e != null || document == null || !document.exists()) return@addSnapshotListener
                val name = document.getString("name") ?: "Anggota"
                val totalSimpanan = document.getDouble("totalSimpanan") ?: 0.0
                onUpdate(name, totalSimpanan)
            }
    }

    // 2. Get Loan Summary (Total Active Debt)
    fun getActiveLoans(userId: String): Task<QuerySnapshot> {
        return db.collection("users").document(userId).collection("loans")
            .whereIn("status", listOf("Disetujui", "Pinjaman Berjalan", "Proses"))
            .get()
    }

    // 3. Get Recent Savings (Transactions)
    fun getRecentSavings(userId: String): Task<QuerySnapshot> {
        return db.collection("users").document(userId).collection("savings")
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(5)
            .get()
    }

    // 4. Get Recent Loans (for history list)
    fun getRecentLoanHistory(userId: String): Task<QuerySnapshot> {
        return db.collection("users").document(userId).collection("loans")
            .orderBy("tanggalPengajuan", Query.Direction.DESCENDING)
            .limit(5)
            .get()
    }
}
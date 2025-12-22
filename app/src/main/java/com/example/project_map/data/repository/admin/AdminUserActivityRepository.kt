package com.example.project_map.data.repository.admin

import com.example.project_map.data.model.Loan
import com.example.project_map.data.model.Savings
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class AdminUserActivityRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getSavingsByUser(userId: String): Task<QuerySnapshot> {
        return db.collectionGroup("savings")
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
    }

    fun getLoansByUser(userId: String): Task<QuerySnapshot> {
        return db.collection("loans")
            .whereEqualTo("userId", userId)
            .orderBy("tanggalPengajuan", Query.Direction.DESCENDING)
            .get()
    }
}
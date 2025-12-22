package com.example.project_map.data.repository.user

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.Date

class UserLaporanRepository {

    private val db = FirebaseFirestore.getInstance()

    fun getMonthlyReportData(userId: String, startDate: Date, endDate: Date): Task<List<QuerySnapshot>> {
        val savingsTask = db.collection("users").document(userId)
            .collection("savings")
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .get()

        val loansTask = db.collection("users").document(userId)
            .collection("loans")
            .whereGreaterThanOrEqualTo("tanggalPengajuan", startDate)
            .whereLessThanOrEqualTo("tanggalPengajuan", endDate)
            .get()

        return Tasks.whenAllSuccess<QuerySnapshot>(savingsTask, loansTask)
    }
}
package com.example.project_map.data.repository.admin

import android.util.Log
import com.example.project_map.data.model.MonthlyFinancialReport
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AdminDashboardRepository {

    private val db = FirebaseFirestore.getInstance()

    // 1. Fetch Total User Savings (Simpanan)
    suspend fun getTotalUserSavings(): Double {
        return try {
            val snapshot = db.collection("users").get().await()
            snapshot.documents.sumOf { it.getDouble("totalSimpanan") ?: 0.0 }
        } catch (e: Exception) {
            Log.e("AdminDashRepo", "Error fetching savings", e)
            0.0
        }
    }

    // 2. Fetch Total Active Loans (Pinjaman)
    suspend fun getTotalActiveLoans(): Double {
        return try {
            val snapshot = db.collectionGroup("loans")
                .whereNotEqualTo("status", "Lunas")
                .get()
                .await()
            snapshot.documents.sumOf { it.getDouble("sisaAngsuran") ?: 0.0 }
        } catch (e: Exception) {
            Log.e("AdminDashRepo", "Error fetching loans", e)
            throw e // Rethrow to let ViewModel handle specific UI error states if needed
        }
    }

    // 3. Fetch Financial Reports for Chart/Summary
    suspend fun getFinancialReports(): List<MonthlyFinancialReport> {
        return try {
            val snapshot = db.collection("financial_reports").get().await()
            snapshot.toObjects(MonthlyFinancialReport::class.java)
        } catch (e: Exception) {
            Log.e("AdminDashRepo", "Error fetching reports", e)
            emptyList()
        }
    }


}
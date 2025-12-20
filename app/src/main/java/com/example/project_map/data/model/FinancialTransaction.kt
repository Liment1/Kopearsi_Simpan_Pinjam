package com.example.project_map.data.model

import com.google.firebase.Timestamp
import java.util.Date

data class FinancialTransaction(
    var id: String = "",
    val amount: Double = 0.0,
    val type: String = "",       // "Pemasukan" or "Pengeluaran"
    val category: String = "",   // "Simpanan Pokok", "Pinjaman", etc.
    val date: Date? = null,      // Ensure Firestore uses Timestamp
    val description: String = ""
)
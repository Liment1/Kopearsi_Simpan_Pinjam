package com.example.project_map.data.model

import java.util.Date

data class FinancialTransaction(
    val id: String = "",
    val type: String = "",       // "Pemasukan" or "Pengeluaran"
    val category: String = "",   // "Simpanan Wajib", "Angsuran", etc.
    val amount: Double = 0.0,
    val date: Date? = null,
    val description: String = ""
)
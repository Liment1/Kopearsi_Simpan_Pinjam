package com.example.project_map.data.model

import com.google.firebase.Timestamp

data class MonthlyFinancialReport(
    var id: String = "",
    val month: String = "",
    val year: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netProfit: Double = 0.0,
    val generatedAt: Timestamp? = null,
    val distributed: Boolean = false
)
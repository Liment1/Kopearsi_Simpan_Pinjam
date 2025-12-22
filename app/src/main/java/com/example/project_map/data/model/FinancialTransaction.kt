package com.example.project_map.data.model

import com.google.firebase.Timestamp
import java.util.Date

data class FinancialTransaction(
    var id: String = "",
    val amount: Double = 0.0,
    val type: String = "",
    val category: String = "",
    val date: Date? = null,
    val description: String = ""
)
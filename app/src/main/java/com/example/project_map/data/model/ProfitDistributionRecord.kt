package com.example.project_map.data.model

import com.google.firebase.Timestamp

data class ProfitDistributionRecord(
    val id: String = "",
    val sourceReportId: String = "", // Link to the financial report doc
    val sourceMonth: String = "",
    val totalRevenue: Double = 0.0,
    val netProfit: Double = 0.0,
    val distributedAmount: Double = 0.0, // 90%
    val sharePerMember: Double = 0.0,
    val totalMembers: Int = 0,
    val distributedAt: Timestamp = Timestamp.now()
)
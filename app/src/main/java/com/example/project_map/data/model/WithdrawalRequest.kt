package com.example.project_map.data.model

import java.util.Date

data class WithdrawalRequest(
    var id: String = "",
    val userId: String = "",
    val userName: String = "",
    val amount: Double = 0.0,
    val bankName: String = "", // Added back
    val accountNumber: String = "",
    val status: String = "Pending",
    val requestDate: Date? = null
)
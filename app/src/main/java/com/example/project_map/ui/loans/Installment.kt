package com.example.project_map.ui.loans

data class Installment(
    val number: Int,
    val type: String,
    val amount: String,
    val date: String,
    val otherFees: String,
    val isPaid: Boolean
)
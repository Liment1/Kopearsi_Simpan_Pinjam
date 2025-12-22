package com.example.project_map.ui.user.home

data class UserRecentItem(
    val title: String,
    val date: String,
    val amount: String,
    val type: TransactionType
)

enum class TransactionType {
    SAVINGS, LOAN, WITHDRAWAL, EXPENSE
}
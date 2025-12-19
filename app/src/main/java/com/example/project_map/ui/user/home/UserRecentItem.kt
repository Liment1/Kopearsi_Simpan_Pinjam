package com.example.project_map.ui.user.home

/**
 * A UI Model representing a single row in the Dashboard history list.
 * This is separate from the Domain models (Loan/Transaction) because
 * it contains pre-formatted strings ready for display.
 */
data class UserRecentItem(
    val title: String,
    val date: String,
    val amount: String
)
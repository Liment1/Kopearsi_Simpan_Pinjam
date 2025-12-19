package com.example.project_map.ui.user.savings

import com.example.project_map.data.model.Savings

/**
 * UI Model for the Savings List.
 * Formatted and ready for display.
 */
data class UserSavingsHistoryItem(
    val originalSavings: Savings,
    val type: String,
    val amountString: String,
    val dateString: String,
    val isExpense: Boolean,
    val imageUrl: String?

)
package com.example.app.savings

enum class TransactionType(val displayName: String) {
    DEPOSIT("Deposit"),
    WITHDRAWAL("Withdraw");

    override fun toString(): String {
        return displayName
    }
}

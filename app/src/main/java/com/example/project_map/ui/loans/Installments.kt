package com.example.project_map.ui.loans

import java.util.Date

// Represents the main Loan Document (Header)
data class LoanDetails(
    var id: String = "",
    var purpose: String = "",
    var status: String = "",
    var principalAmount: Double = 0.0,
    var receivedAmount: Double = 0.0,
    var termMonths: Int = 0,
    var dueDateDesc: String = "" // e.g. "15 Setiap Bulan"
)

// Represents the Sub-collection "installments" (History items)
// Renamed variables slightly to match Firestore best practices (optional),
// but kept class name 'Loans' to match your file name.
data class Loans(
    var number: Int = 0,
    var type: String = "",
    var amount: Double = 0.0, // Changed from String to Double for calculation safety
    var date: Date? = null,   // Changed from String to Date for sorting
    var otherFees: Double = 0.0,
    var isPaid: Boolean = false
)
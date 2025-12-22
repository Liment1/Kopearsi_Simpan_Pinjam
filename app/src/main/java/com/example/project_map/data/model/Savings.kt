package com.example.project_map.data.model

import com.google.firebase.firestore.Exclude
import java.util.Date

data class Savings(
    var id: String = "",

    // --- ADDED THESE FIELDS TO FIX ERRORS ---
    var userId: String = "",
    var userName: String = "",

    var date: Date? = null,
    var type: String = "",
    var amount: Double = 0.0,
    var description: String = "",

    // AdminDetail looks for "proofUrl", while user might save "imageUri".
    // Using proofUrl here to match Admin logic.
    var proofUrl: String = "",
    var imageUri: String? = null, // Kept for backward compatibility if needed

    var status: String = "Selesai" // "Selesai", "Pending", "Ditolak"
)
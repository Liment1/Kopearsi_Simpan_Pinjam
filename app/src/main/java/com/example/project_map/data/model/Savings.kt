package com.example.project_map.data.model

import com.google.firebase.firestore.Exclude
import java.util.Date

data class Savings(
    var id: String = "",

    var userId: String = "",
    var userName: String = "",

    var date: Date? = null,
    var type: String = "",
    var amount: Double = 0.0,
    var description: String = "",

    var proofUrl: String = "",
    var imageUri: String? = null,

    var status: String = "Selesai" // "Selesai", "Pending", "Ditolak"
)
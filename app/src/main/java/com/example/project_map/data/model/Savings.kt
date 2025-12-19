package com.example.project_map.data.model

import java.util.Date

data class Savings(
    var id: String = "",
    // Firestore stores Timestamps, which map to Date in Kotlin
    var date: Date? = null,
    var type: String = "", // "Simpanan Pokok", "Simpanan Wajib", "Simpanan Sukarela"
    var amount: Double = 0.0,
    var description: String = "",
    var imageUri: String? = null
)
package com.example.project_map.data.model

import com.google.firebase.Timestamp

data class Announcement(
    var id: String = "", // Document ID (excluded from Firestore body, set manually)
    val title: String = "",
    val message: String = "",
    val date: Timestamp? = null, // Changed from 'timestamp' to match screenshot
    @field:JvmField // Ensures Firestore maps 'isUrgent' correctly
    val isUrgent: Boolean = false
)
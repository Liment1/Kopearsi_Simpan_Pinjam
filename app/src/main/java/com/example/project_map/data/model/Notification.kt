package com.example.project_map.data.model

import com.google.firebase.Timestamp

data class Notification(
    var id: String = "",
    val title: String = "",
    val message: String = "",
    val date: Timestamp? = null,
    @field:JvmField
    val isUrgent: Boolean = false
)
package com.example.project_map.data.model

import com.google.firebase.firestore.Exclude

data class UserData(
    @get:Exclude var id: String = "",
    var memberCode: String = "",
    var email: String = "",
    var name: String = "",
    var phone: String = "",
    var admin: Boolean = false,
    var status: String = "",
    var avatarUrl: String = "",

    // New Fields for Admin Edit
    var creditScore: Int = 0,
    var simpananPokok: Double = 0.0,
    var simpananWajib: Double = 0.0
)
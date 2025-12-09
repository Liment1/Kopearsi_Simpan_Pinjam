package com.example.project_map.data

import com.google.firebase.firestore.Exclude

data class UserData(
    // Firestore Document ID (Excluded from writing to DB)
    @get:Exclude var id: String = "",

    var memberCode: String = "",
    var email: String = "",
    var name: String = "",
    var phone: String = "",
    var admin: Boolean = false,
    var status: String = "",
    var avatarUrl: String = ""
)
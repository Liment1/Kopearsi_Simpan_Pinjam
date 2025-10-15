package com.example.project_map.data

data class UserData(
    val id: String,
    var email: String, // Diubah ke var
    var pass: String, // Diubah ke var
    var name: String, // Diubah ke var
    var phone: String, // Diubah ke var
    val isAdmin: Boolean = false,
    var status: String // Properti baru ditambahkan
)
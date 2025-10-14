package com.example.project_map.data

data class UserData(
    val id: String,
    val email: String,
    val pass: String,
    val name: String,
    val phone: String,
    val isAdmin: Boolean = false
)
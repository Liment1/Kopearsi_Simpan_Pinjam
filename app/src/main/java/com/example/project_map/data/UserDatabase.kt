package com.example.project_map.data

object UserDatabase {
    val allUsers = mutableListOf(
        UserData("AGT0001", "admin@gmail.com", "admin", "Administrator Utama", "081234567890", true, "Anggota Aktif"),
        UserData("AGT0002", "user@gmail.com", "user", "Budi Santoso", "087654321098", false, "Anggota Aktif"),
        UserData("AGT0003", "siti@gmail.com", "siti123", "Siti Aminah", "089988776655", false, "Calon Anggota"),
        UserData("AGT0004", "doni@gmail.com", "doni1", "Doni Saputra", "081122334455", false, "Anggota Tidak Aktif")
    )
}
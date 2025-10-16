package com.example.project_map.data

object UserDatabase {
    val allUsers = mutableListOf(
        // pass: Admin123_
        UserData("AGT0001", "admin@gmail.com", "ed944070e1402e2eb6f83630063c99d30536f605d25a8e8dec591a57299fb9d0", "Administrator Utama", "081234567890", true, "Anggota Aktif"),
        // pass: User123_ hash: 694d42e50f01fe779f773ae110b93bc3fbf3ef63c01049a195497689b411506c
        UserData("AGT0002", "user@gmail.com", "694d42e50f01fe779f773ae110b93bc3fbf3ef63c01049a195497689b411506c", "Santi Sanjaya", "087654321098", false, "Anggota Aktif"),
        // pass: Siti123_
        UserData("AGT0003", "siti@gmail.com", "d863f1b36b05c12fd2608f49153ab24e46b65a377cfe1c8448e91db1aea81408", "Siti Aminah", "089988776655", false, "Calon Anggota"),
        // pass: Doni123_
        UserData("AGT0004", "doni@gmail.com", "8ad4ce1e5c85a5c6a86a7152e2c8b05c4c43bd652bf72d5c86f6cc2a557b85e9", "Doni Saputra", "081122334455", false, "Anggota Tidak Aktif")
    )
}
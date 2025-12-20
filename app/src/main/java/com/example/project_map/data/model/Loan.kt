package com.example.project_map.data.model

import java.util.Date

data class Loan(
    var id: String = "",
    val userId: String = "",
    val namaPeminjam: String = "",
    val nominal: Double = 0.0,
    val tenor: String = "",
    val tujuan: String = "",
    val status: String = "Proses", // Proses, Disetujui, Ditolak, Lunas
    val bunga: Double = 0.05,
    val sisaAngsuran: Double = 0.0,
    val totalDibayar: Double = 0.0,
    val tanggalPengajuan: Date? = null,
    val alasanPenolakan: String = "",
    val ktpUrl: String = ""
)
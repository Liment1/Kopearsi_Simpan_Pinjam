package com.example.project_map.data.model

import java.util.Date

data class Loan(
    var id: String = "",
    var namaPeminjam: String = "",
    // Matches 'nominal' in Firestore
    var nominal: Double = 0.0,
    // Matches 'tenor' in Firestore
    var tenor: String = "",
    var tujuan: String = "",
    // Matches 'status' in Firestore
    var status: String = "",
    var bunga: Double = 0.05,
    // Matches 'sisaAngsuran' in Firestore
    var sisaAngsuran: Double = 0.0,
    // Matches 'totalDibayar' in Firestore
    var totalDibayar: Double = 0.0,
    var alasanPenolakan: String = "",
    // Matches 'tanggalPengajuan' (Timestamp) in Firestore
    var tanggalPengajuan: Date? = null
)
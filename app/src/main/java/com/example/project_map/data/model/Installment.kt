package com.example.project_map.data.model

import java.util.Date

// This MUST be named 'Installment' to match your Repository and ViewModel imports
data class Installment(
    val id: String = "",
    val loanId: String = "",
    val bulanKe: Int = 0,       // Matches repository logic
    val jumlahBayar: Double = 0.0,
    val jatuhTempo: Date? = null,

    // Status: "Belum Bayar", "Menunggu Konfirmasi", "Lunas", "Telat"
    val status: String = "Belum Bayar",

    val tanggalBayar: Date? = null,
    val buktiBayarUrl: String = ""
)
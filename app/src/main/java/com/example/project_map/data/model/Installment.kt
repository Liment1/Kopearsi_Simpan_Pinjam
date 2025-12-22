package com.example.project_map.data.model

import com.google.firebase.firestore.Exclude
import java.util.Date

data class Installment(
    var id: String = "",
    var loanId: String = "",

    val bulanKe: Int = 1,
    val jumlahBayar: Double = 0.0,
    val jatuhTempo: Date? = null,
    val status: String = "Belum Bayar",
    val tanggalBayar: Date? = null,
    val buktiBayarUrl: String = "",

    // These are filled manually by the Repository
    @get:Exclude var peminjamName: String = "",
    @get:Exclude var userId: String = ""
)
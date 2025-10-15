package com.example.project_map.data

import java.util.Date

// Enum untuk tipe catatan
enum class TipeCatatan {
    SIMPANAN, PINJAMAN, ANGSURAN
}

// Data class untuk satu baris catatan keuangan
data class CatatanKeuangan(
    val date: Date,
    val description: String,
    val amount: Double,
    val type: TipeCatatan
)
    package com.example.project_map.data

import java.util.Date

// Enum untuk tipe catatan
enum class TipeCatatan {
    SIMPANAN,
    ANGSURAN,
    PINJAMAN,
    OPERASIONAL; // <-- ADDED: For operational costs like electricity, supplies, etc.

    fun isPemasukan(): Boolean {
        return this == SIMPANAN || this == ANGSURAN
    }
}

// Data class untuk satu baris catatan keuangan (No changes needed here)
data class CatatanKeuangan(
    val date: Date,
    val description: String,
    val amount: Double,
    val type: TipeCatatan
)

package com.example.project_map.data

import java.util.Calendar
import java.util.Date

object KoperasiDatabase {
    // Daftar semua transaksi keuangan dari semua anggota
    val allTransactions = mutableListOf<CatatanKeuangan>()

    // Kita isi dengan data dummy saat pertama kali diakses
    init {
        createDummyData()
    }

    private fun createDummyData() {
        // Data 2 bulan lalu
        add(getDate(-2, 5), "Simpanan Wajib - Budi S.", 100000.0, TipeCatatan.SIMPANAN)
        add(getDate(-2, 10), "Pinjaman Cair - Doni S.", 1000000.0, TipeCatatan.PINJAMAN)
        add(getDate(-2, 15), "Angsuran #1 - Siti A.", 250000.0, TipeCatatan.ANGSURAN)
        add(getDate(-2, 25), "Biaya Listrik & Air", 350000.0, TipeCatatan.ANGSURAN) // Dicatat sebagai pengeluaran/angsuran

        // Data bulan lalu
        add(getDate(-1, 5), "Simpanan Wajib - Semua", 400000.0, TipeCatatan.SIMPANAN)
        add(getDate(-1, 12), "Pinjaman Cair - Budi S.", 2500000.0, TipeCatatan.PINJAMAN)
        add(getDate(-1, 15), "Angsuran #2 - Siti A.", 250000.0, TipeCatatan.ANGSURAN)
        add(getDate(-1, 20), "Simpanan Sukarela - Admin", 500000.0, TipeCatatan.SIMPANAN)

        // Data bulan ini (Oktober 2025)
        add(getDate(0, 5), "Simpanan Wajib - Semua", 400000.0, TipeCatatan.SIMPANAN)
        add(getDate(0, 10), "Simpanan Sukarela - Doni S.", 150000.0, TipeCatatan.SIMPANAN)
        add(getDate(0, 15), "Angsuran #3 - Siti A.", 250000.0, TipeCatatan.ANGSURAN)
        add(getDate(0, 25), "Biaya ATK & Operasional", 200000.0, TipeCatatan.ANGSURAN) // Dicatat sebagai pengeluaran
    }

    // Helper function
    private fun add(date: Date, desc: String, amount: Double, type: TipeCatatan) {
        allTransactions.add(CatatanKeuangan(date, desc, amount, type))
    }

    private fun getDate(monthOffset: Int, day: Int): Date {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, monthOffset)
        cal.set(Calendar.DAY_OF_MONTH, day)
        return cal.time
    }
}
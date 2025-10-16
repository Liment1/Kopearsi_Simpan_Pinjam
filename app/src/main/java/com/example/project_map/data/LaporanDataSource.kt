package com.example.project_map.data
import com.example.project_map.data.CatatanKeuangan
import com.example.project_map.data.TipeCatatan
import java.util.Calendar
import java.util.Date

/**
 * A singleton object to provide dummy data for the monthly report.
 * This separates the data from the UI logic.
 */
object LaporanDataSource {

    /**
     * Returns a predefined list of financial transactions.
     */
    fun getDummyTransactions(): List<CatatanKeuangan> {
        return listOf(
            CatatanKeuangan(getDate(-2, 5), "Simpanan Wajib", 100000.0, TipeCatatan.SIMPANAN),
            CatatanKeuangan(getDate(-2, 15), "Bayar Angsuran #1", 250000.0, TipeCatatan.ANGSURAN),
            CatatanKeuangan(getDate(-1, 5), "Simpanan Wajib", 100000.0, TipeCatatan.SIMPANAN),
            CatatanKeuangan(getDate(-1, 10), "Pinjaman Renovasi", 2000000.0, TipeCatatan.PINJAMAN),
            CatatanKeuangan(getDate(-1, 15), "Bayar Angsuran #2", 250000.0, TipeCatatan.ANGSURAN),
            CatatanKeuangan(getDate(-1, 20), "Simpanan Sukarela", 50000.0, TipeCatatan.SIMPANAN),
            CatatanKeuangan(getDate(0, 5), "Simpanan Wajib", 100000.0, TipeCatatan.SIMPANAN),
            CatatanKeuangan(getDate(0, 10), "Simpanan Sukarela", 150000.0, TipeCatatan.SIMPANAN)
        )
    }

    /**
     * Helper function to generate a Date object based on a month offset from today.
     */
    private fun getDate(monthOffset: Int, day: Int): Date {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, monthOffset)
        cal.set(Calendar.DAY_OF_MONTH, day)
        return cal.time
    }
}
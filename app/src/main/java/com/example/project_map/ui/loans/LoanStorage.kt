package com.example.project_map.ui.loans

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object LoanStorage {
    private const val PREF_NAME = "loan_data"
    private const val KEY_LOANS = "loans"

    /** Simpan pinjaman baru dengan ID unik */
    fun saveLoan(context: Context, loan: JSONObject) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val array = JSONArray(prefs.getString(KEY_LOANS, "[]"))

        if (!loan.has("id")) {
            loan.put("id", System.currentTimeMillis()) // Gunakan timestamp sebagai ID unik
        }

        array.put(loan)
        prefs.edit().putString(KEY_LOANS, array.toString()).apply()
    }

    /** Ambil semua pinjaman dari SharedPreferences */
    fun getAllLoans(context: Context): List<JSONObject> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val array = JSONArray(prefs.getString(KEY_LOANS, "[]"))
        val list = mutableListOf<JSONObject>()
        for (i in 0 until array.length()) {
            list.add(array.getJSONObject(i))
        }
        return list
    }

    /** Update pinjaman berdasarkan ID */
    fun updateLoan(context: Context, updatedLoan: JSONObject) {
        val loans = getAllLoans(context).toMutableList()
        val targetId = updatedLoan.optLong("id", -1)

        if (targetId == -1L) return  // Tidak bisa update jika ID tidak valid

        val updatedList = loans.map { loan ->
            if (loan.optLong("id", -1) == targetId) updatedLoan else loan
        }

        saveAllLoans(context, updatedList)
    }

    /** Simpan ulang seluruh daftar pinjaman */
    fun saveAllLoans(context: Context, loans: List<JSONObject>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val array = JSONArray()
        loans.forEach { array.put(it) }
        prefs.edit().putString(KEY_LOANS, array.toString()).apply()
    }

    /** Hapus pinjaman berdasarkan ID */
    fun deleteLoan(context: Context, id: Long) {
        val loans = getAllLoans(context)
        val filteredLoans = loans.filterNot { it.optLong("id", -1) == id }
        saveAllLoans(context, filteredLoans)
    }
}

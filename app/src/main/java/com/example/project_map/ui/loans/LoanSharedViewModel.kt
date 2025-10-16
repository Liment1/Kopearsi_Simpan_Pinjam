package com.example.project_map.ui.loans

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.json.JSONObject

class LoanSharedViewModel : ViewModel() {

    // daftar semua loan (LiveData agar UI bisa update otomatis)
    private val _loanList = MutableLiveData<MutableList<Loan>>(mutableListOf())
    val loanList: LiveData<MutableList<Loan>> get() = _loanList

    // fungsi menambahkan pinjaman baru (dipanggil dari form pengajuan)
    fun addLoan(loan: Loan) {
        val currentList = _loanList.value ?: mutableListOf()
        currentList.add(loan)
        _loanList.value = currentList
    }

    // fungsi update status pinjaman (dipanggil oleh admin)
    fun updateLoanStatus(context: Context, id: Long, newStatus: String) {
        val currentList = _loanList.value
        currentList?.let { list ->
            val loan = list.find { it.id == id }
            loan?.let { l ->
                l.status = newStatus
                _loanList.value = list // update LiveData supaya UI refresh

                // Simpan perubahan ke SharedPreferences
                val json = JSONObject().apply {
                    put("id", l.id)
                    put("namaPeminjam", l.namaPeminjam)
                    put("nominal", l.nominal)
                    put("tenor", l.tenor)
                    put("tujuan", l.tujuan)
                    put("status", l.status)
                    put("bunga", l.bunga)
                    put("sisaAngsuran", l.sisaAngsuran)
                    put("totalDibayar", l.totalDibayar)
                }
                LoanStorage.updateLoan(context, json)
            }
        }
    }
}

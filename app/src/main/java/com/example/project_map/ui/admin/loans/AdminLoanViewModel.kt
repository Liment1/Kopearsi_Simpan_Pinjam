package com.example.project_map.ui.admin.loans

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.project_map.data.model.Loan
import com.example.project_map.data.repository.admin.AdminLoanRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AdminLoanViewModel : ViewModel() {

    private val repository = AdminLoanRepository()

    private val _allLoans = MutableLiveData<List<Loan>>()

    // Filter for "Proses" (Case insensitive check is safer)
    val pendingLoans: LiveData<List<Loan>> = _allLoans.map { list ->
        list.filter { it.status.equals("Proses", ignoreCase = true) }
    }

    val historyLoans: LiveData<List<Loan>> = _allLoans.map { list ->
        list.filter { !it.status.equals("Proses", ignoreCase = true) }
    }

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    init {
        fetchLoans()
    }

    private fun fetchLoans() {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getAllLoansStream()
                .catch { e ->
                    // --- ERROR HANDLING RESTORED ---
                    _isLoading.value = false
                    _message.value = "Gagal memuat: ${e.message}"
                    Log.e("AdminLoanVM", "Error in flow", e)
                }
                .collect { list ->
                    Log.d("AdminLoanVM", "Received ${list.size} loans. Checking for missing names...")

                    // --- FIX: FILL MISSING NAMES ---
                    // Since 'namaPeminjam' is empty in your DB, we fetch it from 'users'
                    val updatedList = list.map { loan ->
                        if (loan.namaPeminjam.isEmpty() && loan.userId.isNotEmpty()) {
                            val name = repository.getUserName(loan.userId)
                            loan.copy(namaPeminjam = name) // Return copy with name filled
                        } else {
                            loan
                        }
                    }

                    _allLoans.value = updatedList
                    _isLoading.value = false
                    Log.d("AdminLoanVM", "Data updated with names. Total: ${updatedList.size}")
                }
        }
    }



    fun approveLoan(loan: Loan) {
        viewModelScope.launch {
            // FIX: Pass loan.userId here
            val result = repository.approveLoan(loan.id, loan.userId)
            if (result.isSuccess) _message.value = "Pinjaman disetujui"
            else _message.value = "Gagal: ${result.exceptionOrNull()?.message}"
        }
    }

    fun rejectLoan(loan: Loan, reason: String) {
        viewModelScope.launch {
            // FIX: Pass loan.userId here
            val result = repository.rejectLoan(loan.id, loan.userId, reason)
            if (result.isSuccess) _message.value = "Pinjaman ditolak"
            else _message.value = "Gagal: ${result.exceptionOrNull()?.message}"
        }
    }

    fun onMessageShown() {
        _message.value = null
    }
}
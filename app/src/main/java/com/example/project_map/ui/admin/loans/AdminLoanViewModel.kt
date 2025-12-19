package com.example.project_map.ui.admin.loans

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.project_map.data.model.Loan
import com.example.project_map.data.repository.admin.AdminLoanRepository
import com.google.firebase.firestore.util.Logger.debug
import kotlinx.coroutines.launch
import android.util.Log
// import kotlinx.coroutines.flow.catch  <-- You can remove this import

class AdminLoanViewModel : ViewModel() {

    private val repository = AdminLoanRepository()

    private val _allLoans = MutableLiveData<List<Loan>>()

    val pendingLoans: LiveData<List<Loan>> = _allLoans.map { list ->
        list.filter { it.status == "Proses" }
    }

    val historyLoans: LiveData<List<Loan>> = _allLoans.map { list ->
        list.filter { it.status != "Proses" }
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
            // --- MODIFIED: REMOVED .catch{} BLOCK ---
            // This will cause the app to CRASH if the index is missing.
            // Look for the "FATAL EXCEPTION" in Logcat to find the blue link.
            repository.getAllLoansStream()
                .collect { list ->
                    _allLoans.value = list
                    _isLoading.value = false
                }
        }
    }

    fun approveLoan(loan: Loan) {
        viewModelScope.launch {
            val result = repository.approveLoan(loan.id)
            if (result.isSuccess) _message.value = "Pinjaman disetujui"
            else {
                _message.value = "Gagal: ${result.exceptionOrNull()?.message}"
                Log.e("RejectLoan", "Gagal: ${result.exceptionOrNull()?.message}")

            }
        }
    }

    fun rejectLoan(loan: Loan, reason: String) {
        viewModelScope.launch {
            val result = repository.rejectLoan(loan.id, reason)
            if (result.isSuccess) _message.value = "Pinjaman ditolak"
            else{
                _message.value = "Gagal: ${result.exceptionOrNull()?.message}"
            }

        }
    }

    fun onMessageShown() {
        _message.value = null
    }
}
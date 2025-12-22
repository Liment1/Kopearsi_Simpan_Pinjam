package com.example.project_map.ui.admin.loans

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_map.data.model.Loan
import com.example.project_map.data.model.UserData
import com.example.project_map.data.repository.admin.AdminLoanRepository
import kotlinx.coroutines.launch

class AdminLoanDetailViewModel : ViewModel() {
    private val repository = AdminLoanRepository()

    private val _loan = MutableLiveData<Loan?>()
    val loan: LiveData<Loan?> = _loan

    private val _user = MutableLiveData<UserData?>()
    val user: LiveData<UserData?> = _user

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    private val _navigateBack = MutableLiveData<Boolean>()
    val navigateBack: LiveData<Boolean> = _navigateBack

    fun loadData(loanId: String, userId: String) {
        viewModelScope.launch {
            val loanResult = repository.getLoanDetail(loanId, userId)
            if (loanResult.isSuccess) {
                _loan.value = loanResult.getOrNull()
            } else {
                _message.value = "Gagal memuat pinjaman: ${loanResult.exceptionOrNull()?.message}"
            }

            if (userId.isNotEmpty()) {
                val userResult = repository.getUserDetail(userId)
                if (userResult.isSuccess) _user.value = userResult.getOrNull()
            }
        }
    }

    // Standard update (Approve)
    fun updateStatus(loanId: String, userId: String, status: String) {
        viewModelScope.launch {
            val result = repository.updateLoanStatus(loanId, userId, status)
            if (result.isSuccess) {
                _message.value = "Pinjaman $status"
                _navigateBack.value = true
            } else {
                _message.value = "Gagal: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun rejectLoan(loanId: String, userId: String, reason: String) {
        viewModelScope.launch {
            val result = repository.rejectLoan(loanId, userId, reason)
            if (result.isSuccess) {
                _message.value = "Pinjaman ditolak"
                _navigateBack.value = true
            } else {
                _message.value = "Gagal: ${result.exceptionOrNull()?.message}"
            }
        }
    }
}
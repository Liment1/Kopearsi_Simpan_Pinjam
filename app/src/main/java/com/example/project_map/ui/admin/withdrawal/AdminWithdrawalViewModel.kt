package com.example.project_map.ui.admin.withdrawal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_map.data.model.WithdrawalRequest
import com.example.project_map.data.repository.admin.AdminWithdrawalRepository
import kotlinx.coroutines.launch

class AdminWithdrawalViewModel : ViewModel() {

    private val repository = AdminWithdrawalRepository()

    private val _requests = MutableLiveData<List<WithdrawalRequest>>()
    val requests: LiveData<List<WithdrawalRequest>> = _requests

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        fetchRequests()
    }

    private fun fetchRequests() {
        viewModelScope.launch {
            repository.getPendingWithdrawals().collect { list ->
                _requests.value = list
            }
        }
    }

    fun approve(request: WithdrawalRequest) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.approveWithdrawal(request)
            _isLoading.value = false
            if (result.isSuccess) _message.value = "Penarikan Disetujui"
            else _message.value = "Gagal: ${result.exceptionOrNull()?.message}"
        }
    }

    fun reject(request: WithdrawalRequest) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.rejectWithdrawal(request.id)
            _isLoading.value = false
            if (result.isSuccess) _message.value = "Penarikan Ditolak"
            else _message.value = "Gagal: ${result.exceptionOrNull()?.message}"
        }
    }
}
package com.example.project_map.ui.admin.installments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.project_map.data.model.Installment
import com.example.project_map.data.repository.admin.AdminInstallmentRepository

class AdminInstallmentViewModel : ViewModel() {

    private val repository = AdminInstallmentRepository()

    private val _installments = MutableLiveData<List<Installment>>()
    val installments: LiveData<List<Installment>> = _installments

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadData()
    }

    private fun loadData() {
        _isLoading.value = true

        // FIX: Changed from 'listenToPendingInstallments' to 'listenToPaidInstallments'
        repository.listenToPaidInstallments(
            onSuccess = { list ->
                _installments.postValue(list)
                _isLoading.postValue(false)
            },
            onError = { e ->
                _errorMessage.postValue("Gagal memuat: ${e.message}")
                _isLoading.postValue(false)
            }
        )
    }

    // NOTE: Removed approve/reject functions because we are now only viewing the history log.
}
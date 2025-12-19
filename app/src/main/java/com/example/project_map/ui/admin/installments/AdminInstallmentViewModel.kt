package com.example.project_map.ui.admin.installments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_map.data.model.Installment
import com.example.project_map.data.repository.admin.AdminInstallmentRepository
import kotlinx.coroutines.launch

class AdminInstallmentViewModel : ViewModel() {

    private val repository = AdminInstallmentRepository()

    private val _installments = MutableLiveData<List<Installment>>()
    val installments: LiveData<List<Installment>> = _installments

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        fetchInstallments()
    }

    private fun fetchInstallments() {
        _isLoading.value = true
        viewModelScope.launch {
          repository.getAllInstallmentsStream().collect { list ->
                _installments.value = list
                _isLoading.value = false
            }
        }
    }
}
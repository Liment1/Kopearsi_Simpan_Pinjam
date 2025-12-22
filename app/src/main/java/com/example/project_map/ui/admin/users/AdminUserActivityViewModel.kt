package com.example.project_map.ui.admin.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.project_map.data.model.Loan
import com.example.project_map.data.model.Savings
import com.example.project_map.data.repository.admin.AdminUserActivityRepository

class AdminUserActivityViewModel : ViewModel() {
    private val repository = AdminUserActivityRepository()

    private val _savingsList = MutableLiveData<List<Savings>>()
    val savingsList: LiveData<List<Savings>> = _savingsList

    private val _loanList = MutableLiveData<List<Loan>>()
    val loanList: LiveData<List<Loan>> = _loanList

    // Menyimpan state loading jika perlu
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadSavings(userId: String) {
        _isLoading.value = true
        repository.getSavingsByUser(userId)
            .addOnSuccessListener { result ->
                val list = result.toObjects(Savings::class.java)
                _savingsList.value = list
                _isLoading.value = false
            }
            .addOnFailureListener {
                _isLoading.value = false
                // Handle error
            }
    }

    fun loadLoans(userId: String) {
        _isLoading.value = true
        repository.getLoansByUser(userId)
            .addOnSuccessListener { result ->
                val list = result.toObjects(Loan::class.java)
                _loanList.value = list
                _isLoading.value = false
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }
}
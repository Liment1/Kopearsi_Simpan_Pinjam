package com.example.project_map.ui.admin.savings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_map.data.model.Savings
import com.example.project_map.data.model.UserData
import com.example.project_map.data.repository.admin.AdminSavingsRepository
import kotlinx.coroutines.launch

class AdminSavingsDetailViewModel : ViewModel() {
    private val repository = AdminSavingsRepository()

    private val _savings = MutableLiveData<Savings?>()
    val savings: LiveData<Savings?> = _savings

    private val _user = MutableLiveData<UserData?>()
    val user: LiveData<UserData?> = _user

    fun loadData(transId: String, userId: String) {
        viewModelScope.launch {
            val transResult = repository.getTransactionDetail(transId, userId)
            _savings.value = transResult.getOrNull()

            if (userId.isNotEmpty()) {
                val userResult = repository.getUserDetail(userId)
                _user.value = userResult.getOrNull()
            }
        }
    }
}
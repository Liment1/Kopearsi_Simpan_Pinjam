//package com.example.project_map.ui.admin.cashflow
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.project_map.data.repository.admin.AdminFinancialRepository
//import com.example.project_map.data.repository.admin.Transaction
//import kotlinx.coroutines.launch
//
//class AdminCashFlowViewModel : ViewModel() {
//
//    private val repository = AdminFinancialRepository()
//
//    private val _transactions = MutableLiveData<List<Transaction>>()
//    val transactions: LiveData<List<Transaction>> = _transactions
//
//    private val _totalBalance = MutableLiveData<Double>()
//    val totalBalance: LiveData<Double> = _totalBalance
//
//    private val _isLoading = MutableLiveData<Boolean>()
//    val isLoading: LiveData<Boolean> = _isLoading
//
//    init {
//        fetchCashFlow()
//    }
//
//    private fun fetchCashFlow() {
//        _isLoading.value = true
//        viewModelScope.launch {
//            repository.getCashFlowStream().collect { list ->
//                _transactions.value = list
//
//                // Calculate Total Balance (Income - Expense)
//                val income = list.filter { it.type == "Pemasukan" }.sumOf { it.amount }
//                val expense = list.filter { it.type == "Pengeluaran" }.sumOf { it.amount }
//                _totalBalance.value = income - expense
//
//                _isLoading.value = false
//            }
//        }
//    }
//}
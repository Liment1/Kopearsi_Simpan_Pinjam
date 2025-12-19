package com.example.project_map.ui.admin.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_map.data.model.MonthlyFinancialReport
import com.example.project_map.data.repository.admin.AdminDashboardRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class AdminDashboardViewModel : ViewModel() {

    private val repository = AdminDashboardRepository()

    // LiveData for Summary Cards
    private val _totalSavings = MutableLiveData<Double>()
    val totalSavings: LiveData<Double> = _totalSavings

    private val _totalActiveLoans = MutableLiveData<Double>()
    val totalActiveLoans: LiveData<Double> = _totalActiveLoans

    private val _totalRevenue = MutableLiveData<Double>()
    val totalRevenue: LiveData<Double> = _totalRevenue

    private val _totalProfit = MutableLiveData<Double>()
    val totalProfit: LiveData<Double> = _totalProfit

    // LiveData for Chart
    private val _chartData = MutableLiveData<List<MonthlyFinancialReport>>()
    val chartData: LiveData<List<MonthlyFinancialReport>> = _chartData

    // UI State
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                // Run fetches in parallel for efficiency
                val savingsDeferred = async { repository.getTotalUserSavings() }
                val loansDeferred = async { repository.getTotalActiveLoans() }
                val reportsDeferred = async { repository.getFinancialReports() }

                // Await results
                val savings = savingsDeferred.await()
                val loans = loansDeferred.await()
                val reports = reportsDeferred.await()

                // Update LiveData
                _totalSavings.value = savings
                _totalActiveLoans.value = loans

                // Calculate Totals from reports
                val revenueSum = reports.sumOf { it.totalRevenue }
                val profitSum = reports.sumOf { it.netProfit }

                _totalRevenue.value = revenueSum
                _totalProfit.value = profitSum
                _chartData.value = reports

            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
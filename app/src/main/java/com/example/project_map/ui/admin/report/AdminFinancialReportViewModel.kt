package com.example.project_map.ui.admin.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_map.data.model.FinancialTransaction
import com.example.project_map.data.repository.admin.AdminFinancialRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Calendar

class AdminFinancialReportViewModel : ViewModel() {

    private val repository = AdminFinancialRepository()
    private var allTransactions: List<FinancialTransaction> = emptyList()

    // State
    private var currentCalendar: Calendar = Calendar.getInstance()
    private var isMonthlyView: Boolean = true

    // UI State
    private val _reportState = MutableLiveData<ReportState>()
    val reportState: LiveData<ReportState> = _reportState

    private val _currentPeriodLabel = MutableLiveData<String>()
    val currentPeriodLabel: LiveData<String> = _currentPeriodLabel

    init {
        loadData()
        updatePeriodLabel()
    }

    private fun loadData() {
        _reportState.value = ReportState.Loading
        viewModelScope.launch {
            repository.getCashFlowStream().collect { list ->
                allTransactions = list
                recalculateReport()
            }
        }
    }

    private fun recalculateReport() {
        val filteredList = filterTransactions(allTransactions)

        // 1. Calculate Totals
        val totalIncome = filteredList.filter { it.type == "Pemasukan" }.sumOf { it.amount }
        val totalExpense = filteredList.filter { it.type == "Pengeluaran" }.sumOf { it.amount }
        val netProfit = totalIncome - totalExpense

        // 2. Calculate Breakdown (Rincian)
        // Income Categories
        val incSimpanan = filteredList.filter {
            it.type == "Pemasukan" && it.category.contains("Simpanan", ignoreCase = true)
        }.sumOf { it.amount }

        val incAngsuran = filteredList.filter {
            it.type == "Pemasukan" && it.category.contains("Angsuran", ignoreCase = true)
        }.sumOf { it.amount }

        val incDenda = filteredList.filter {
            it.type == "Pemasukan" && it.category.contains("Denda", ignoreCase = true)
        }.sumOf { it.amount }

        // Expense Categories
        val expPinjaman = filteredList.filter {
            it.type == "Pengeluaran" &&
                    (it.category.contains("Pinjaman", ignoreCase = true) || it.category.contains("Pencairan", ignoreCase = true))
        }.sumOf { it.amount }

        // Assume anything else in Expense is "Operasional"
        val expOperasional = totalExpense - expPinjaman

        _reportState.value = ReportState.Success(
            income = totalIncome,
            expense = totalExpense,
            netProfit = netProfit,
            // Breakdown
            incSimpanan = incSimpanan,
            incAngsuran = incAngsuran,
            incDenda = incDenda,
            expPinjaman = expPinjaman,
            expOperasional = expOperasional
        )
    }

    private fun filterTransactions(list: List<FinancialTransaction>): List<FinancialTransaction> {
        return list.filter { item ->
            val itemDate = item.date ?: return@filter false
            val itemCal = Calendar.getInstance().apply { time = itemDate }

            if (isMonthlyView) {
                itemCal.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                        itemCal.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)
            } else {
                itemCal.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)
            }
        }
    }

    // --- Actions ---
    fun setReportType(isMonthly: Boolean) {
        if (isMonthlyView != isMonthly) {
            isMonthlyView = isMonthly
            updatePeriodLabel()
            recalculateReport()
        }
    }

    fun nextPeriod() {
        if (isMonthlyView) currentCalendar.add(Calendar.MONTH, 1) else currentCalendar.add(Calendar.YEAR, 1)
        updatePeriodLabel()
        recalculateReport()
    }

    fun prevPeriod() {
        if (isMonthlyView) currentCalendar.add(Calendar.MONTH, -1) else currentCalendar.add(Calendar.YEAR, -1)
        updatePeriodLabel()
        recalculateReport()
    }

    private fun updatePeriodLabel() {
        val monthNames = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")

        if (isMonthlyView) {
            val month = monthNames[currentCalendar.get(Calendar.MONTH)]
            val year = currentCalendar.get(Calendar.YEAR)
            _currentPeriodLabel.value = "$month $year"
        } else {
            _currentPeriodLabel.value = "${currentCalendar.get(Calendar.YEAR)}"
        }
    }

    // --- State ---
    sealed class ReportState {
        object Loading : ReportState()
        data class Success(
            val income: Double,
            val expense: Double,
            val netProfit: Double,
            // Detailed Breakdown
            val incSimpanan: Double,
            val incAngsuran: Double,
            val incDenda: Double,
            val expPinjaman: Double,
            val expOperasional: Double
        ) : ReportState()
    }
}
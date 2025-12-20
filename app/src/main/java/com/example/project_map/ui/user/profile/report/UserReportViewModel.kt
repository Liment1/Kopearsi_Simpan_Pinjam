package com.example.project_map.ui.user.profile.report

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.project_map.data.model.Loan
import com.example.project_map.data.model.Savings
import com.example.project_map.data.repository.user.UserLaporanRepository
import com.google.firebase.firestore.QuerySnapshot
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UserReportViewModel : ViewModel() {

    private val repository = UserLaporanRepository()
    private val calendar = Calendar.getInstance()

    // Observables
    private val _monthTitle = MutableLiveData<String>()
    val monthTitle: LiveData<String> = _monthTitle

    private val _financialSummary = MutableLiveData<FinancialSummary>()
    val financialSummary: LiveData<FinancialSummary> = _financialSummary

    private val _transactionList = MutableLiveData<List<UserItemHistory>>()
    val transactionList: LiveData<List<UserItemHistory>> = _transactionList

    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty

    init {
        updateDateAndFetch()
    }

    // Called by Activity when pressing Next/Prev buttons
    fun changeMonth(offset: Int) {
        calendar.add(Calendar.MONTH, offset)
        updateDateAndFetch()
    }

    private fun updateDateAndFetch() {
        // 1. Update Title (e.g., "Desember 2025")
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale("in", "ID"))
        _monthTitle.value = dateFormat.format(calendar.time)

        // 2. Calculate Start and End Date of the month
        val startDate = calendar.clone() as Calendar
        startDate.set(Calendar.DAY_OF_MONTH, 1)
        startDate.set(Calendar.HOUR_OF_DAY, 0)
        startDate.set(Calendar.MINUTE, 0)
        startDate.set(Calendar.SECOND, 0)

        val endDate = calendar.clone() as Calendar
        endDate.set(Calendar.DAY_OF_MONTH, endDate.getActualMaximum(Calendar.DAY_OF_MONTH))
        endDate.set(Calendar.HOUR_OF_DAY, 23)
        endDate.set(Calendar.MINUTE, 59)
        endDate.set(Calendar.SECOND, 59)

        fetchData(startDate.time, endDate.time)
    }

    private fun fetchData(startDate: java.util.Date, endDate: java.util.Date) {
        // Call Repository
        repository.getMonthlyReportData(startDate, endDate)
            .addOnSuccessListener { results ->
                val savingsSnapshot = results[0] as QuerySnapshot
                val loansSnapshot = results[1] as QuerySnapshot
                // val installmentsSnapshot = results[2] as QuerySnapshot (If implemented)

                val uiList = mutableListOf<UserItemHistory>()
                var totalSimpanan = 0.0
                var totalPinjaman = 0.0
                var totalAngsuran = 0.0 // Placeholder

                val displayFormat = SimpleDateFormat("dd MMM, HH:mm", Locale("in", "ID"))

                // --- Process Savings ---
                for (doc in savingsSnapshot) {
                    val item = doc.toObject(Savings::class.java)
                    // Assuming 'amount' is Double and 'type' is String
                    val amount = item.amount

                    // Logic: Green for Deposit, Red for Withdrawal
                    val isDeposit = item.type.contains("Simpanan", ignoreCase = true)
                    val color = if (isDeposit) Color.parseColor("#388E3C") else Color.parseColor("#D32F2F")
                    val prefix = if (isDeposit) "+ " else "- "

                    if (isDeposit) totalSimpanan += amount else totalSimpanan -= amount

                    uiList.add(
                        UserItemHistory(
                            date = if(item.date != null) displayFormat.format(item.date) else "-",
                            description = item.type,
                            amount = "$prefix${formatRupiah(amount)}",
                            color = color
                        )
                    )
                }

                // --- Process Loans ---
                for (doc in loansSnapshot) {
                    val loan = doc.toObject(Loan::class.java)
                    totalPinjaman += loan.nominal

                    uiList.add(
                        UserItemHistory(
                            date = if(loan.tanggalPengajuan != null) displayFormat.format(loan.tanggalPengajuan) else "-",
                            description = "Pinjaman: ${loan.tujuan}",
                            amount = "+ ${formatRupiah(loan.nominal)}",
                            color = Color.parseColor("#1976D2") // Blue for Loans
                        )
                    )
                }

                // --- Finalize UI ---
                // Sort by Date (We need raw date for sorting, parsing string is risky,
                // but simpler for now is to insert at top or just rely on List logic.
                // Ideally, Models should track raw date. Here we just add them.)

                _financialSummary.value = FinancialSummary(
                    totalSimpanan = formatRupiah(totalSimpanan),
                    totalPinjaman = formatRupiah(totalPinjaman),
                    totalAngsuran = formatRupiah(totalAngsuran)
                )

                _isEmpty.value = uiList.isEmpty()
                _transactionList.value = uiList.reversed() // Show newest first if added sequentially
            }
            .addOnFailureListener {
                // Handle error (optional: _isEmpty.value = true)
                _isEmpty.value = true
            }
    }

    private fun formatRupiah(number: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(number)
    }
}
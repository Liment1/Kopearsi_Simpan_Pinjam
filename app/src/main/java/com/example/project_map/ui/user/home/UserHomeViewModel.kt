package com.example.project_map.ui.user.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.project_map.data.model.Savings
import com.example.project_map.data.model.Loan
import com.example.project_map.data.repository.user.UserHomeRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// We use AndroidViewModel to get access to "Application" context for string resources/locales if needed,
// but simple ViewModel is also fine if context isn't strictly needed.
class UserHomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserHomeRepository()
    private val auth = FirebaseAuth.getInstance()
    private var userListener: ListenerRegistration? = null

    // LiveData for UI State
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _totalBalance = MutableLiveData<String>()
    val totalBalance: LiveData<String> = _totalBalance

    private val _totalLoanDebt = MutableLiveData<String>()
    val totalLoanDebt: LiveData<String> = _totalLoanDebt

    // Assuming you have a class 'RecentActivity' used by your adapter
    // If RecentActivity is defined inside HomeFragment, move it to a separate file.
    private val _recentActivities = MutableLiveData<List<UserRecentItem>>()
    val recentActivities: LiveData<List<UserRecentItem>> = _recentActivities

    init {
        loadData()
    }

    fun loadData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            setupUserListener(uid)
            fetchLoanSummary(uid)
            fetchCombinedActivity(uid)
        } else {
            _userName.value = "Tamu"
            _totalBalance.value = "Rp 0"
        }
    }

    private fun setupUserListener(userId: String) {
        userListener = repository.addUserListener(userId) { name, balance ->
            _userName.value = name
            _totalBalance.value = formatCurrency(balance)
        }
    }

    private fun fetchLoanSummary(userId: String) {
        repository.getActiveLoans(userId)
            .addOnSuccessListener { result ->
                var totalSisa = 0.0
                for (doc in result) {
                    val loan = doc.toObject(Loan::class.java)
                    totalSisa += loan.sisaAngsuran
                }
                _totalLoanDebt.value = formatCurrency(totalSisa)
            }
            .addOnFailureListener {
                _totalLoanDebt.value = "Rp 0"
            }
    }

    private fun fetchCombinedActivity(userId: String) {
        val savingsTask = repository.getRecentSavings(userId)
        val loansTask = repository.getRecentLoanHistory(userId)

        Tasks.whenAllSuccess<Any>(savingsTask, loansTask)
            .addOnSuccessListener { results ->
                val rawItems = mutableListOf<DashboardItem>()

                // 1. Process Savings
                val savingsSnapshot = results[0] as com.google.firebase.firestore.QuerySnapshot
                for (doc in savingsSnapshot) {
                    val item = doc.toObject(Savings::class.java)
                    val isExp = item.type.contains("Penarikan")
                    val prefix = if (isExp) "-" else "+"
                    rawItems.add(DashboardItem(
                        title = item.type,
                        date = item.date,
                        amountString = "$prefix ${formatCurrency(item.amount)}"
                    ))
                }

                // 2. Process Loans
                val loansSnapshot = results[1] as com.google.firebase.firestore.QuerySnapshot
                for (doc in loansSnapshot) {
                    val item = doc.toObject(Loan::class.java)
                    rawItems.add(DashboardItem(
                        title = "Pinjaman: ${item.tujuan}",
                        date = item.tanggalPengajuan,
                        amountString = "+ ${formatCurrency(item.nominal)}"
                    ))
                }

                // 3. Sort, Limit, and Map to Adapter Model
                val sdf = SimpleDateFormat("dd MMM", Locale("in", "ID"))
                val sortedList = rawItems
                    .sortedByDescending { it.date }
                    .take(5)
                    .map {
                        UserRecentItem(
                            title = it.title,
                            date = if (it.date != null) sdf.format(it.date) else "-",
                            amount = it.amountString
                        )
                    }

                _recentActivities.value = sortedList
            }
            .addOnFailureListener { e ->
                Log.e("HomeViewModel", "Error fetching combined activity", e)
            }
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0
        return formatter.format(amount)
    }

    override fun onCleared() {
        super.onCleared()
        userListener?.remove()
    }

    // Helper data class for sorting before mapping to UI model
    private data class DashboardItem(
        val title: String,
        val date: Date?,
        val amountString: String
    )
}
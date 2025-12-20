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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserHomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserHomeRepository()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // UI Data
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _userStatus = MutableLiveData<String>()
    val userStatus: LiveData<String> = _userStatus

    private val _totalBalance = MutableLiveData<String>()
    val totalBalance: LiveData<String> = _totalBalance

    private val _totalLoanDebt = MutableLiveData<String>()
    val totalLoanDebt: LiveData<String> = _totalLoanDebt

    private val _unreadNotifCount = MutableLiveData<Int>()
    val unreadNotifCount: LiveData<Int> = _unreadNotifCount

    private val _recentActivities = MutableLiveData<List<UserRecentItem>>()
    val recentActivities: LiveData<List<UserRecentItem>> = _recentActivities

    init {
        loadData()
    }

    fun loadData() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            fetchUserProfile(uid)
            fetchLoanSummary(uid)
            fetchCombinedActivity(uid)
            checkUnreadNotifications(uid)
        } else {
            _userName.value = "Tamu"
            _userStatus.value = "Tidak Aktif"
            _totalBalance.value = "Rp 0"
            _totalLoanDebt.value = "Rp 0"
        }
    }

    private fun fetchUserProfile(uid: String) {
        db.collection("users").document(uid).addSnapshotListener { doc, _ ->
            if (doc != null && doc.exists()) {
                _userName.value = doc.getString("name") ?: "Anggota"
                _userStatus.value = doc.getString("status") ?: "Calon Anggota"

                val balance = doc.getDouble("totalSimpanan") ?: 0.0
                _totalBalance.value = formatCurrency(balance)
            }
        }
    }

    private fun checkUnreadNotifications(uid: String) {
        db.collection("users").document(uid).get().addOnSuccessListener { userDoc ->
            val lastRead = userDoc.getTimestamp("lastReadAnnouncementDate") ?: Timestamp(0, 0)

            db.collection("announcements")
                .whereGreaterThan("date", lastRead)
                .get()
                .addOnSuccessListener { snapshot ->
                    _unreadNotifCount.value = snapshot.size()
                }
                .addOnFailureListener {
                    _unreadNotifCount.value = 0
                }
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
            .addOnFailureListener { _totalLoanDebt.value = "Rp 0" }
    }

    private fun fetchCombinedActivity(userId: String) {
        val savingsTask = repository.getRecentSavings(userId)
        val loansTask = repository.getRecentLoanHistory(userId)

        Tasks.whenAllSuccess<Any>(savingsTask, loansTask).addOnSuccessListener { results ->
            val rawItems = mutableListOf<DashboardItem>()

            // 1. Process Savings (Income/Expense)
            val savingsSnapshot = results[0] as com.google.firebase.firestore.QuerySnapshot
            for (doc in savingsSnapshot) {
                val item = doc.toObject(Savings::class.java)

                val isMinus = item.type.contains("Penarikan") || item.type.contains("Pengeluaran")
                val prefix = if (isMinus) "-" else "+"

                rawItems.add(DashboardItem(item.type, item.date, "$prefix ${formatCurrency(item.amount)}"))
            }

            // 2. Process Loans (Incoming money)
            val loansSnapshot = results[1] as com.google.firebase.firestore.QuerySnapshot
            for (doc in loansSnapshot) {
                val item = doc.toObject(Loan::class.java)
                rawItems.add(DashboardItem("Pinjaman: ${item.tujuan}", item.tanggalPengajuan, "+ ${formatCurrency(item.nominal)}"))
            }

            val sdf = SimpleDateFormat("dd MMM", Locale("in", "ID"))
            val sortedList = rawItems
                .sortedByDescending { it.date }
                .take(5)
                .map { dashboardItem ->
                    val dateStr = if (dashboardItem.date != null) sdf.format(dashboardItem.date) else "-"

                    // --- LOGIC TO DETERMINE TYPE ---
                    val type = when {
                        dashboardItem.title.contains("Simpanan", ignoreCase = true) -> TransactionType.SAVINGS
                        dashboardItem.title.contains("Pinjaman", ignoreCase = true) -> TransactionType.LOAN
                        dashboardItem.title.contains("Penarikan", ignoreCase = true) -> TransactionType.WITHDRAWAL
                        else -> TransactionType.EXPENSE
                    }

                    UserRecentItem(dashboardItem.title, dateStr, dashboardItem.amount, type)
                }

            _recentActivities.value = sortedList
        }
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0
        return formatter.format(amount)
    }

    // Helper class for sorting
    private data class DashboardItem(val title: String, val date: Date?, val amount: String)
}
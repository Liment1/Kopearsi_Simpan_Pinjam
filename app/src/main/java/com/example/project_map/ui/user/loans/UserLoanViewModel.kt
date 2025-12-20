package com.example.project_map.ui.user.loans

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_map.data.model.Installment
import com.example.project_map.data.model.Loan
import com.example.project_map.data.repository.user.UserLoanRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

class UserLoanViewModel : ViewModel() {

    private val repository = UserLoanRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val currentUserId get() = auth.currentUser?.uid

    // --- DASHBOARD STATES ---
    private val _activeLoans = MutableLiveData<List<Loan>>()
    val activeLoans: LiveData<List<Loan>> = _activeLoans

    private val _totalDebt = MutableLiveData<String>()
    val totalDebt: LiveData<String> = _totalDebt

    // --- DETAIL & INSTALLMENT STATES (Missing Part) ---
    private val _installments = MutableLiveData<List<Installment>>() // <--- ADDED
    val installments: LiveData<List<Installment>> = _installments    // <--- ADDED

    private val _loanDetails = MutableLiveData<Loan?>()
    val loanDetails: LiveData<Loan?> = _loanDetails

    // --- APPLY/PAY STATES ---
    private val _creditScore = MutableLiveData<Double?>()
    val creditScore: LiveData<Double?> = _creditScore

    private val _loanState = MutableLiveData<State<Unit>>()
    val loanState: LiveData<State<Unit>> = _loanState

    init {
        fetchActiveLoans()
    }

    // 1. Dashboard: Listen to Active Loans
    private fun fetchActiveLoans() {
        val uid = currentUserId ?: return

        db.collection("users").document(uid).collection("loans")
            .whereIn("status", listOf("Disetujui", "Berjalan", "Proses")) // Filter active ones
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener

                val list = value?.toObjects(Loan::class.java) ?: emptyList()
                _activeLoans.value = list

                // Calculate Total Debt (Sum of sisaAngsuran)
                val total = list.sumOf { it.sisaAngsuran }
                val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                format.maximumFractionDigits = 0
                _totalDebt.value = format.format(total)
            }
    }

    // 2. Apply: Fetch Score
    fun fetchScore() {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            val score = repository.getCreditScore(uid)
            _creditScore.value = score
        }
    }

    // 3. Apply: Submit Loan
    fun submitLoan(
        jenis: String, peruntukan: String, lama: Int, satuan: String, nominal: Double,  ktpUri: Uri?
    ) {
        val uid = currentUserId ?: return
        val name = auth.currentUser?.displayName ?: "User"

        // Validation inside ViewModel
        if (ktpUri == null) {
            _loanState.value = State.Error("Foto KTP wajib diunggah")
            return
        }

        _loanState.value = State.Loading

        val bunga = nominal * 0.05
        val total = nominal + bunga

        val loan = Loan(
            nominal = nominal,
            tenor = "$lama $satuan",
            tujuan = "$jenis - $peruntukan", // Combine Type and Purpose
            status = "Proses",
            bunga = 0.05,
            sisaAngsuran = total,
            totalDibayar = 0.0,
            tanggalPengajuan = Date()
            // Ensure your Loan model has a 'ktpUrl' field, or add it.
        )

        viewModelScope.launch {
            try {
                // You will need to update repository.createLoan to handle the Uri
                // If repository doesn't support it yet, you might need to upload it here or inside repo
                repository.createLoan(uid, name, loan, lama, ktpUri)
                _loanState.value = State.Success(Unit)
            } catch (e: Exception) {
                _loanState.value = State.Error(e.message ?: "Gagal mengajukan")
            }
        }
    }

    // --- NEW: Load Installments for a specific loan ---
    fun loadInstallments(loanId: String) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            try {
                // Call repository to get list
                val list = repository.getInstallments(uid, loanId)
                _installments.value = list
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    // 4. Pay: Process Payment
    fun payInstallment(installment: Installment, method: String, proofUri: Uri?) {
        val uid = currentUserId ?: return
        _loanState.value = State.Loading

        viewModelScope.launch {
            try {
                if (method == "SALDO") {
                    repository.payInstallmentViaBalance(uid, installment)
                } else {
                    if (proofUri == null) throw Exception("Bukti transfer wajib diupload")
                    repository.payInstallment(uid, installment, proofUri)
                }
                _loanState.value = State.Success(Unit)
                // Refresh list after payment
                loadInstallments(installment.loanId)
            } catch (e: Exception) {
                val errorMsg = if (e.message?.contains("Saldo") == true) "Saldo tidak mencukupi!" else e.message
                _loanState.value = State.Error(errorMsg ?: "Pembayaran gagal")
            }
        }
    }

    fun loadLoanDetails(loanId: String) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            try {
                val loan = repository.getLoan(uid, loanId)
                _loanDetails.value = loan
            } catch (e: Exception) { }
        }
    }

    fun resetState() {
        _loanState.value = State.Idle
    }

    sealed class State<out T> {
        object Idle : State<Nothing>()
        object Loading : State<Nothing>()
        data class Success<T>(val data: T) : State<T>()
        data class Error(val message: String) : State<Nothing>()
    }
}
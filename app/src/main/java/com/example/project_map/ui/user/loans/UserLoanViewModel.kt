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
import kotlinx.coroutines.launch
import java.util.Date

class UserLoanViewModel : ViewModel() {

    private val repository = UserLoanRepository()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId get() = auth.currentUser?.uid

    // --- STATES ---
    private val _creditScore = MutableLiveData<Double?>()
    val creditScore: LiveData<Double?> = _creditScore

    private val _loanState = MutableLiveData<State<Unit>>() // For create/pay actions
    val loanState: LiveData<State<Unit>> = _loanState

    private val _installments = MutableLiveData<List<Installment>>()
    val installments: LiveData<List<Installment>> = _installments

    // --- ACTIONS ---

    fun fetchScore() {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            val score = repository.getCreditScore(uid)
            _creditScore.value = score
        }
    }

    fun submitLoan(
        jenis: String, peruntukan: String, lama: Int, satuan: String, nominal: Double
    ) {
        val uid = currentUserId ?: return
        val name = auth.currentUser?.displayName ?: "User"

        _loanState.value = State.Loading

        val bunga = nominal * 0.05 // 5% Rate
        val total = nominal + bunga

        val loan = Loan(
            nominal = nominal,
            tenor = "$lama $satuan",
            tujuan = peruntukan,
            status = "Proses",
            bunga = 0.05,
            sisaAngsuran = total,
            totalDibayar = 0.0,
            tanggalPengajuan = Date()
        )

        viewModelScope.launch {
            try {
                repository.createLoan(uid, name, loan, lama) // Assuming monthly for simplicity
                _loanState.value = State.Success(Unit)
            } catch (e: Exception) {
                _loanState.value = State.Error(e.message ?: "Gagal mengajukan")
            }
        }
    }

    fun loadInstallments(loanId: String) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            val list = repository.getInstallments(uid, loanId)
            _installments.value = list
        }
    }

    // ... inside LoanViewModel class

    // Updated pay function to handle both methods
    fun payInstallment(installment: Installment, method: String, proofUri: Uri?) {
        val uid = currentUserId ?: return
        _loanState.value = State.Loading

        viewModelScope.launch {
            try {
                if (method == "SALDO") {
                    // Logic for Potong Saldo
                    repository.payInstallmentViaBalance(uid, installment)
                } else {
                    // Logic for Transfer (requires URI)
                    if (proofUri == null) throw Exception("Bukti transfer wajib diupload")
                    repository.payInstallment(uid, installment, proofUri)
                }
                _loanState.value = State.Success(Unit)
            } catch (e: Exception) {
                val errorMsg = if (e.message?.contains("Saldo") == true) "Saldo tidak mencukupi!" else e.message
                _loanState.value = State.Error(errorMsg ?: "Pembayaran gagal")
            }
        }
    }

    // State for the Loan Header Details
    private val _loanDetails = MutableLiveData<Loan?>()
    val loanDetails: LiveData<Loan?> = _loanDetails

    fun loadLoanDetails(loanId: String) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            try {
                val loan = repository.getLoan(uid, loanId)
                _loanDetails.value = loan
            } catch (e: Exception) {
                // Handle error (optional)
            }
        }
    }

    fun resetState() {
        _loanState.value = State.Idle
    }


    // Helper State Class
    sealed class State<out T> {
        object Idle : State<Nothing>() // <--- Add this
        object Loading : State<Nothing>()
        data class Success<T>(val data: T) : State<T>()
        data class Error(val message: String) : State<Nothing>()
    }
}
package com.example.project_map.ui.user.loans

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.project_map.data.model.Installment
import com.example.project_map.data.model.Loan
import com.example.project_map.data.repository.user.UserLoanRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class UserLoanViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserLoanRepository()
    private val auth = FirebaseAuth.getInstance()
    private var loansListener: ListenerRegistration? = null

    // --- State ---
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Success : State()
        data class Error(val message: String) : State()
    }
    private val _loanState = MutableLiveData<State>(State.Idle)
    val loanState: LiveData<State> = _loanState

    // --- Data Streams ---
    private val _activeLoans = MutableLiveData<List<Loan>>()
    val activeLoans: LiveData<List<Loan>> = _activeLoans

    private val _totalDebt = MutableLiveData<String>("Rp 0")
    val totalDebt: LiveData<String> = _totalDebt

    private val _installments = MutableLiveData<List<Installment>>()
    val installments: LiveData<List<Installment>> = _installments

    private val _activeLoan = MutableLiveData<Loan?>()
    val activeLoan: LiveData<Loan?> = _activeLoan

    private val _actionResult = MutableLiveData<Result<String>?>()
    val actionResult: LiveData<Result<String>?> = _actionResult

    // --- NEW: Credit Score LiveData ---
    private val _creditScore = MutableLiveData<Double?>()
    val creditScore: LiveData<Double?> = _creditScore

    init {
        startLoanStream()
    }

    private fun startLoanStream() {
        val uid = auth.currentUser?.uid ?: return
        loansListener = repository.getActiveLoansStream(uid) { loans ->
            _activeLoans.value = loans
            val total = loans.sumOf { it.sisaAngsuran }
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            formatter.maximumFractionDigits = 0
            _totalDebt.value = formatter.format(total)
        }
    }

    // --- NEW: Function to Refresh Score ---
    fun refreshCreditScore() {
        val uid = auth.currentUser?.uid ?: return

        // Optional: Set loading state if you want to show a spinner specifically for this
        _loanState.value = State.Loading

        viewModelScope.launch {
            try {
                // Calls the repository function that wraps CreditScoreManager
                val score = repository.getCreditScore(uid)
                _creditScore.value = score
                _loanState.value = State.Idle
            } catch (e: Exception) {
                _loanState.value = State.Error("Gagal mengambil skor kredit")
            }
        }
    }

    fun fetchInstallments(loanId: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val list = repository.getInstallments(uid, loanId)
            _installments.value = list
            val loan = repository.getLoan(uid, loanId)
            _activeLoan.value = loan
        }
    }

    fun submitLoan(jenis: String, peruntukan: String, amount: Double, tenor: Int, satuan: String, ktpUri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        val name = auth.currentUser?.displayName ?: "Anggota"

        viewModelScope.launch {
            try {
                val loan = Loan(
                    userId = uid,
                    namaPeminjam = name,
                    nominal = amount,
                    tenor = "$tenor $satuan",
                    tujuan = "$jenis - $peruntukan",
                    sisaAngsuran = amount + (amount * 0.05),
                    totalDibayar = 0.0,
                    status = "Proses"
                )
                repository.createLoan(uid, name, loan, tenor, ktpUri)
                _actionResult.value = Result.success("Berhasil")
            } catch (e: Exception) {
                _actionResult.value = Result.failure(e)
            }
        }
    }

    fun payInstallment(installment: Installment, method: String, proofUri: Uri?) {
        val uid = auth.currentUser?.uid ?: return
        _loanState.value = State.Loading

        viewModelScope.launch {
            try {
                if (method == "SALDO") {
                    repository.payInstallmentViaBalance(uid, installment)
                } else {
                    if (proofUri == null) throw Exception("Mohon sertakan bukti transfer.")
                    repository.payInstallment(uid, installment, proofUri)
                }
                _loanState.value = State.Success
                fetchInstallments(installment.loanId)
            } catch (e: Exception) {
                _loanState.value = State.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun resetState() { _loanState.value = State.Idle }
    fun resetResult() { _actionResult.value = null }

    override fun onCleared() {
        super.onCleared()
        loansListener?.remove()
    }
}
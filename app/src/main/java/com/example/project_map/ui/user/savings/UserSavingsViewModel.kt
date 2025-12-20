package com.example.project_map.ui.user.savings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.project_map.data.model.Transaction
import com.example.project_map.data.repository.user.SavingsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class UserSavingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SavingsRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _balances = MutableLiveData<Map<String, String>>()
    val balances: LiveData<Map<String, String>> = _balances

    private val _userStatus = MutableLiveData<String>()
    val userStatus: LiveData<String> = _userStatus

    private val _history = MutableLiveData<List<Transaction>>()
    val history: LiveData<List<Transaction>> = _history

    private val _actionResult = MutableLiveData<Result<String>?>()
    val actionResult: LiveData<Result<String>?> = _actionResult

    init {
        loadData()
    }

    private fun loadData() {
        val uid = auth.currentUser?.uid ?: return

        // 1. Status
        db.collection("users").document(uid).addSnapshotListener { doc, _ ->
            _userStatus.value = doc?.getString("status") ?: "Calon Anggota"
        }

        // 2. Balances
        repository.getUserBalanceStream(uid) { dataMap ->
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            formatter.maximumFractionDigits = 0
            val formatted = dataMap.mapValues { formatter.format(it.value) }
            _balances.value = formatted
        }

        // 3. History
        viewModelScope.launch {
            repository.getSavingsHistory(uid).collect { list ->
                _history.value = list
            }
        }
    }

    fun submitDeposit(amount: Double) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener {
            val name = it.getString("name") ?: ""
            viewModelScope.launch {
                val result = repository.requestDeposit(uid, name, amount)
                if (result.isSuccess) _actionResult.value = Result.success("Permintaan Deposit berhasil dikirim.")
                else _actionResult.value = Result.failure(result.exceptionOrNull()!!)
            }
        }
    }

    // Update signature to accept bankName
    fun submitWithdrawal(amount: Double, bankName: String, account: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener {
            val name = it.getString("name") ?: ""
            viewModelScope.launch {
                // Pass bankName to repository
                val result = repository.requestWithdrawal(uid, name, amount, bankName, account)
                if (result.isSuccess) _actionResult.value = Result.success("Permintaan Penarikan berhasil dikirim.")
                else _actionResult.value = Result.failure(result.exceptionOrNull()!!)
            }
        }
    }
    fun resetResult() { _actionResult.value = null }
}
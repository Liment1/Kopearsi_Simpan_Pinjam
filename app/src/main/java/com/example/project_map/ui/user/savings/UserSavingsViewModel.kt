package com.example.project_map.ui.user.savings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.project_map.data.model.Savings
import com.example.project_map.data.repository.user.SavingsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class UserSavingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SavingsRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _balances = MutableLiveData<Map<String, String>>()
    val balances: LiveData<Map<String, String>> = _balances

    private val _userStatus = MutableLiveData<String>()
    val userStatus: LiveData<String> = _userStatus

    private val _history = MutableLiveData<List<Savings>>()
    val history: LiveData<List<Savings>> = _history

    private val _actionResult = MutableLiveData<Result<String>?>()
    val actionResult: LiveData<Result<String>?> = _actionResult

    init {
        loadData()
    }

    private fun loadData() {
        val uid = auth.currentUser?.uid ?: return

        // 1. Updated to use the new getUserStream
        repository.getUserStream(uid) { data ->
            // Update Status
            _userStatus.value = data.status

            // Update Balances with Formatting
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            formatter.maximumFractionDigits = 0

            // 2. FIX: Map keys to add "Formatted" suffix so UI can find them
            val formattedMap = data.balances.mapKeys { "${it.key}Formatted" }
                .mapValues { formatter.format(it.value) }

            _balances.value = formattedMap
        }

        // History Stream
        repository.getSavingsHistory(uid) { list ->
            _history.value = list
        }
    }

    fun submitDeposit(amount: Double) {
        val uid = auth.currentUser?.uid ?: return
        val name = auth.currentUser?.displayName ?: "Anggota"

        viewModelScope.launch {
            val result = repository.requestDeposit(uid, name, amount)
            if (result.isSuccess) _actionResult.value = Result.success("Permintaan Deposit berhasil.")
            else _actionResult.value = Result.failure(result.exceptionOrNull()!!)
        }
    }

    // 3. Added Missing Function
    fun submitWithdrawal(amount: Double, bankName: String, accountNumber: String) {
        val uid = auth.currentUser?.uid ?: return
        val name = auth.currentUser?.displayName ?: "Anggota"

        viewModelScope.launch {
            val result = repository.requestWithdrawal(uid, name, amount, bankName, accountNumber)
            if (result.isSuccess) _actionResult.value = Result.success("Permintaan Penarikan berhasil.")
            else _actionResult.value = Result.failure(result.exceptionOrNull()!!)
        }
    }

    // 4. Added Missing Function
    fun resetResult() {
        _actionResult.value = null
    }
}
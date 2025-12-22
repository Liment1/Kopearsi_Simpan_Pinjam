package com.example.project_map.ui.user.savings

import android.app.Application
import android.net.Uri
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

    // Added missing Loading state used by Fragment
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadData()
    }

    private fun loadData() {
        val uid = auth.currentUser?.uid ?: return

        repository.getUserStream(uid) { data ->
            _userStatus.value = data.status

            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            formatter.maximumFractionDigits = 0

            val formattedMap = data.balances.mapKeys { "${it.key}Formatted" }
                .mapValues { formatter.format(it.value) }

            _balances.value = formattedMap
        }

        repository.getSavingsHistory(uid) { list ->
            _history.value = list
        }
    }

    // FIX: Updated to accept Uri
    fun submitDeposit(amount: Double, proofUri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        val name = auth.currentUser?.displayName ?: "Anggota"

        _isLoading.value = true
        viewModelScope.launch {
            // Pass the URI to the repository
            val result = repository.requestDeposit(uid, name, amount, proofUri)

            if (result.isSuccess) _actionResult.value = Result.success("Permintaan Deposit berhasil.")
            else _actionResult.value = Result.failure(result.exceptionOrNull()!!)

            _isLoading.value = false
        }
    }

    fun submitWithdrawal(amount: Double, bankName: String, accountNumber: String) {
        val uid = auth.currentUser?.uid ?: return
        val name = auth.currentUser?.displayName ?: "Anggota"

        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.requestWithdrawal(uid, name, amount, bankName, accountNumber)

            if (result.isSuccess) _actionResult.value = Result.success("Permintaan Penarikan berhasil.")
            else _actionResult.value = Result.failure(result.exceptionOrNull()!!)

            _isLoading.value = false
        }
    }

    fun resetResult() {
        _actionResult.value = null
    }
}
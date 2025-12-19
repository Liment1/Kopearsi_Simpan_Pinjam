package com.example.project_map.ui.user.savings

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_map.data.repository.user.SavingsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class UserSavingsViewModel : ViewModel() {

    private val repository = SavingsRepository()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    // --- State: Balances ---
    private val _balances = MutableLiveData<Map<String, String>>()
    val balances: LiveData<Map<String, String>> = _balances

    // --- State: Savings List (Using UI Model, not raw Transaction) ---
    // FIXED: Changed type from List<Transaction> to List<SavingsHistoryItem>
    private val _savingsList = MutableLiveData<List<UserSavingsHistoryItem>>()
    val savingsList: LiveData<List<UserSavingsHistoryItem>> = _savingsList

    // --- State: Loading/Status ---
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    // Internal hold of data for filtering
    private var allSavingsItems: List<UserSavingsHistoryItem> = emptyList()
    private var currentFilter: String = "Semua"

    private var balanceListener: ListenerRegistration? = null
    private var historyListener: ListenerRegistration? = null

    init {
        if (userId != null) {
            setupListeners(userId)
        }
    }

    private fun setupListeners(uid: String) {
        // 1. Balance Listener
        balanceListener = repository.getUserBalanceStream(uid) { data ->
            val formatted = data.mapValues { formatRupiah(it.value) }
            _balances.postValue(formatted)
        }

        // 2. History Listener
        historyListener = repository.getTransactionStream(uid) { rawList ->
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("in", "ID"))

            // Map Domain Model -> UI Model
            val uiList = rawList.map { t ->
                // Fix for unsafe content:// URIs
                val safeUri = if (t.imageUri != null && t.imageUri!!.startsWith("content://")) null else t.imageUri

                // Determine if it is an expense (Money OUT)
                // Adjust this string check based on your exact Firestore data types
                val isExp = t.type.contains("Pembayaran", ignoreCase = true) ||
                        t.type.contains("Penarikan", ignoreCase = true)

                val prefix = if (isExp) "- " else "+ "

                UserSavingsHistoryItem(
                    originalSavings = t,
                    type = t.type,
                    amountString = "$prefix${formatRupiah(t.amount)}",
                    dateString = if (t.date != null) dateFormat.format(t.date!!) else "-",
                    imageUrl = safeUri,
                    isExpense = isExp // <--- FIXED: Now passing the missing parameter
                )
            }

            allSavingsItems = uiList
            applyFilter(currentFilter)
        }
    }

    fun applyFilter(filterType: String) {
        currentFilter = filterType
        if (filterType == "Semua") {
            _savingsList.value = allSavingsItems
        } else {
            _savingsList.value = allSavingsItems.filter { it.type == filterType }
        }
    }

    fun uploadTransaction(amount: Double, type: String, localUri: Uri) {
        if (userId == null) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.uploadAndSaveTransaction(userId, amount, type, localUri)
                _toastMessage.value = "Berhasil disimpan!"
            } catch (e: Exception) {
                _toastMessage.value = "Gagal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun formatRupiah(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }

    override fun onCleared() {
        super.onCleared()
        balanceListener?.remove()
        historyListener?.remove()
    }
}
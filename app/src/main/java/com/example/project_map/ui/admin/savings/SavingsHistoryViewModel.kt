package com.example.project_map.ui.admin.savings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.project_map.data.model.Savings
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlin.jvm.java

class SavingsHistoryViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // Holds the raw data from Firestore
    private val _allSavingss = mutableListOf<Savings>()

    // Exposes the filtered list to the UI
    private val _filteredSavingss = MutableLiveData<List<Savings>>()
    val filteredSavingss: LiveData<List<Savings>> = _filteredSavingss

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        fetchAllSavings()
    }

    // Fetch all savings from every user (Admin View)
    private fun fetchAllSavings() {
        _isLoading.value = true
        db.collectionGroup("savings")
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                _allSavingss.clear()
                for (doc in result) {
                    try {
                        val trans = doc.toObject(Savings::class.java)
                        _allSavingss.add(trans)
                    } catch (e: Exception) {
                        // Skip malformed data
                    }
                }
                // Initial filter is "All"
                applyFilter("Semua")
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Gagal memuat data: ${e.message}"
                _isLoading.value = false
            }
    }

    // Filter logic resides in ViewModel
    fun applyFilter(type: String) {
        if (type == "Semua") {
            _filteredSavingss.value = _allSavingss
        } else {
            _filteredSavingss.value = _allSavingss.filter { it.type == type }
        }
    }
}
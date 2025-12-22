package com.example.project_map.ui.admin.savings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.project_map.data.model.Savings
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SavingsHistoryViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _allSavingss = mutableListOf<Savings>()
    private val _filteredSavingss = MutableLiveData<List<Savings>>()
    val filteredSavingss: LiveData<List<Savings>> = _filteredSavingss

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        fetchAllSavings()
    }

    private fun fetchAllSavings() {
        _isLoading.value = true
        db.collectionGroup("savings")
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                _allSavingss.clear()
                val tempSavings = mutableListOf<Savings>()

                for (doc in result) {
                    try {
                        val item = doc.toObject(Savings::class.java)
                        item.id = doc.id
                        tempSavings.add(item)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // FIX: If userName is empty, fetch it from 'users' collection
                fetchMissingNames(tempSavings) { updatedList ->
                    _allSavingss.addAll(updatedList)
                    applyFilter("Semua")
                    _isLoading.value = false
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Gagal memuat: ${e.message}"
                _isLoading.value = false
            }
    }

    private fun fetchMissingNames(list: List<Savings>, onComplete: (List<Savings>) -> Unit) {
        val usersToFetch = list.filter { it.userName.isEmpty() && it.userId.isNotEmpty() }.map { it.userId }.distinct()

        if (usersToFetch.isEmpty()) {
            onComplete(list)
            return
        }

        // Batch fetch isn't directly supported for IDs easily, so we do it one by one or 'in' query (limit 10)
        // For simplicity here, we assume a small number or fetch individually.
        // Better approach: Store userName in Savings document when creating it.

        var completed = 0
        usersToFetch.forEach { uid ->
            db.collection("users").document(uid).get().addOnSuccessListener { userDoc ->
                val name = userDoc.getString("name") ?: "Unknown"
                // Update all savings with this UID
                list.filter { it.userId == uid }.forEach { it.userName = name }

                completed++
                if (completed == usersToFetch.size) {
                    onComplete(list)
                }
            }
        }
    }

    fun applyFilter(type: String) {
        if (type == "Semua") {
            _filteredSavingss.value = _allSavingss
        } else {
            _filteredSavingss.value = _allSavingss.filter { it.type == type }
        }
    }
}
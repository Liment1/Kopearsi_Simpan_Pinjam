package com.example.project_map.ui.admin.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_map.data.model.UserData
import com.example.project_map.data.repository.admin.AdminUserRepository
import com.example.project_map.data.repository.admin.MemberFinancials
import kotlinx.coroutines.launch

class AdminUserViewModel : ViewModel() {

    private val repository = AdminUserRepository()

    // List of Members
    private val _members = MutableLiveData<List<UserData>>()
    val members: LiveData<List<UserData>> = _members

    // Selected Member's Financial Data (for Dialog)
    private val _selectedMemberFinancials = MutableLiveData<MemberFinancials?>()
    val selectedMemberFinancials: LiveData<MemberFinancials?> = _selectedMemberFinancials

    // UI States
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    init {
        fetchMembers()
    }

    private fun fetchMembers() {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getMembersStream().collect { list ->
                _members.value = list
                _isLoading.value = false
            }
        }
    }

    fun updateMember(uid: String, name: String, phone: String, status: String) {
        viewModelScope.launch {
            val result = repository.updateMember(uid, name, phone, status)
            if (result.isSuccess) {
                _message.value = "Data berhasil diperbarui"
            } else {
                _message.value = "Gagal update: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    // Called when clicking a user to show the summary dialog
    fun loadMemberFinancials(uid: String) {
        // Clear previous data while loading
        _selectedMemberFinancials.value = null

        viewModelScope.launch {
            val result = repository.getMemberFinancials(uid)
            if (result.isSuccess) {
                _selectedMemberFinancials.value = result.getOrNull()
            } else {
                _message.value = "Gagal memuat data keuangan"
            }
        }
    }

    fun onMessageShown() {
        _message.value = null
    }
}
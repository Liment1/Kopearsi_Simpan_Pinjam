package com.example.project_map.ui.admin.notification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_map.data.model.Announcement
import com.example.project_map.data.repository.admin.AdminNotificationRepository
import kotlinx.coroutines.launch

class AdminNotificationViewModel : ViewModel() {

    private val repository = AdminNotificationRepository()

    // Data List
    private val _announcements = MutableLiveData<List<Announcement>>()
    val announcements: LiveData<List<Announcement>> = _announcements

    // UI States
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    init {
        fetchHistory()
    }

    private fun fetchHistory() {
        viewModelScope.launch {
            repository.getAnnouncementHistory().collect { list ->
                _announcements.value = list
            }
        }
    }

    fun sendNotification(title: String, message: String, isUrgent: Boolean) {
        if (title.isBlank() || message.isBlank()) {
            _toastMessage.value = "Isi semua bidang"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.createAnnouncement(title, message, isUrgent)
            _isLoading.value = false
            if (result.isSuccess) {
                _toastMessage.value = "Pengumuman terkirim"
            } else {
                _toastMessage.value = "Gagal: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun distributeProfit() {
        _isLoading.value = true
        _toastMessage.value = "Memproses pembagian..."

        viewModelScope.launch {
            val result = repository.distributeProfit()
            _isLoading.value = false

            result.onSuccess { msg -> _toastMessage.value = msg }
            result.onFailure { e -> _toastMessage.value = "Gagal: ${e.message}" }
        }
    }

    fun clearMessage() {
        _toastMessage.value = null
    }
}
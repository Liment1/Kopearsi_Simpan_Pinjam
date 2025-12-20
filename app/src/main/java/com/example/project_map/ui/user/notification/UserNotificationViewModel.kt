package com.example.project_map.ui.user.notification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_map.data.model.Notification
import com.example.project_map.data.repository.user.UserNotificationRepository
import kotlinx.coroutines.launch
import kotlin.collections.isNotEmpty

class UserNotificationViewModel : ViewModel() {

    private val repository = UserNotificationRepository()

    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> = _notifications

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        fetchNotifications()
    }

    private fun fetchNotifications() {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getNotifications().collect { list ->
                _notifications.value = list
                _isLoading.value = false

                // Automatically mark as read when list is loaded
                if (list.isNotEmpty()) {
                    repository.markNotificationsAsRead()
                }
            }
        }
    }
}
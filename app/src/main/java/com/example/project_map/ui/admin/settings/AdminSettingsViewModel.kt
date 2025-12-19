package com.example.project_map.ui.admin.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_map.data.repository.admin.AdminSettingsRepository
import kotlinx.coroutines.launch

class AdminSettingsViewModel : ViewModel() {

    private val repository = AdminSettingsRepository()

    // State for UI
    private val _settingsState = MutableLiveData<SettingsState>()
    val settingsState: LiveData<SettingsState> = _settingsState

    fun loadSettings() {
        _settingsState.value = SettingsState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getGlobalSettings()
                val interest = data["defaultInterest"] ?: 0.0
                val fine = data["lateFinePercentage"] ?: 0.0

                // Convert decimal back to percentage for display (e.g., 0.05 -> 5)
                _settingsState.value = SettingsState.Loaded(
                    (interest * 100).toInt(),
                    (fine * 100).toInt()
                )
            } catch (e: Exception) {
                _settingsState.value = SettingsState.Error(e.message ?: "Gagal memuat pengaturan")
            }
        }
    }

    fun saveSettings(bungaInput: String, dendaInput: String) {
        if (bungaInput.isEmpty() && dendaInput.isEmpty()) {
            _settingsState.value = SettingsState.Error("Tidak ada perubahan")
            return
        }

        viewModelScope.launch {
            _settingsState.value = SettingsState.Loading
            val updates = hashMapOf<String, Any>()

            try {
                // Convert percentage input to decimal for storage (e.g., 5 -> 0.05)
                if (bungaInput.isNotEmpty()) {
                    updates["defaultInterest"] = bungaInput.toDouble() / 100.0
                }
                if (dendaInput.isNotEmpty()) {
                    updates["lateFinePercentage"] = dendaInput.toDouble() / 100.0
                }

                repository.updateGlobalSettings(updates)
                _settingsState.value = SettingsState.Saved

                // Refresh data to update hints
                loadSettings()
            } catch (e: Exception) {
                _settingsState.value = SettingsState.Error("Gagal menyimpan: ${e.message}")
            }
        }
    }

    sealed class SettingsState {
        object Loading : SettingsState()
        object Saved : SettingsState()
        data class Loaded(val interestPercent: Int, val finePercent: Int) : SettingsState()
        data class Error(val message: String) : SettingsState()
    }
}
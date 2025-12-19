package com.example.project_map.ui.auth.register

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_map.data.repository.user.UserAuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val repository = UserAuthRepository()

    // 1. Global State (Loading & Navigation)
    private val _registerState = MutableLiveData<RegisterState>()
    val registerState: LiveData<RegisterState> = _registerState

    // 2. Field Errors (Specific red text for each input)
    val nameError = MutableLiveData<String?>()
    val phoneError = MutableLiveData<String?>()
    val emailError = MutableLiveData<String?>()
    val passwordError = MutableLiveData<String?>()
    val confirmPasswordError = MutableLiveData<String?>()
    val termsError = MutableLiveData<Boolean>() // True if error exists

    fun register(name: String, phone: String, email: String, pass: String, confirmPass: String, termsAccepted: Boolean) {
        // Reset Errors
        nameError.value = null
        phoneError.value = null
        emailError.value = null
        passwordError.value = null
        confirmPasswordError.value = null
        termsError.value = false

        var isValid = true

        // --- Validation Logic ---
        if (name.isBlank()) {
            nameError.value = "Full Name is required"
            isValid = false
        }

        if (phone.isBlank()) {
            phoneError.value = "Phone Number is required"
            isValid = false
        }

        if (email.isBlank()) {
            emailError.value = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError.value = "Invalid email format"
            isValid = false
        }

        if (pass.isBlank()) {
            passwordError.value = "Password is required"
            isValid = false
        } else if (pass.length < 6) {
            passwordError.value = "Password must be at least 6 characters"
            isValid = false
        }

        if (confirmPass != pass) {
            confirmPasswordError.value = "Passwords do not match"
            isValid = false
        }

        if (!termsAccepted) {
            termsError.value = true // Will trigger a Snackbar or red checkbox
            isValid = false
        }

        if (!isValid) return

        // --- API Call ---
        _registerState.value = RegisterState.Loading

        viewModelScope.launch {
            val result = repository.registerUser(name, phone, email, pass)

            when (result) {
                is UserAuthRepository.AuthResult.Success -> _registerState.value = RegisterState.Success
                is UserAuthRepository.AuthResult.Error -> _registerState.value = RegisterState.Error(result.message)
            }
        }
    }

    sealed class RegisterState {
        object Idle : RegisterState()
        object Loading : RegisterState()
        object Success : RegisterState()
        data class Error(val message: String) : RegisterState()
    }
}
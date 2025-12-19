package com.example.project_map.ui.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Patterns
import com.example.project_map.data.repository.user.UserAuthRepository
import com.example.project_map.data.repository.user.UserAuthRepository.LoginResult
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val repository = UserAuthRepository()

    // 1. General State
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    // 2. Field Validation State
    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> = _emailError

    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> = _passwordError

    fun login(email: String, pass: String) {
        // Reset previous errors
        _emailError.value = null
        _passwordError.value = null

        var isValid = true

        // --- A. Email Validation ---
        if (email.isBlank()) {
            _emailError.value = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Checks for "@", ".", and correct structure
            _emailError.value = "Please enter a valid email address"
            isValid = false
        }

        // --- B. Password Validation ---
        if (pass.isBlank()) {
            _passwordError.value = "Password is required"
            isValid = false
        } else if (pass.length < 6) {
            // Enforce minimum security length
            _passwordError.value = "Password must be at least 6 characters"
            isValid = false
        }

        // If any validation failed, stop here. Do not call Firebase.
        if (!isValid) return

        // --- C. Server Call ---
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val result = repository.login(email, pass)

            when (result) {
                is LoginResult.SuccessAdmin -> _loginState.value = LoginState.NavigateToAdmin
                is LoginResult.SuccessUser -> _loginState.value = LoginState.NavigateToHome
                is LoginResult.Error -> {
                    _loginState.value = LoginState.Error(result.message)
                }
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object NavigateToHome : LoginState()
        object NavigateToAdmin : LoginState()
        data class Error(val message: String) : LoginState()
    }
}
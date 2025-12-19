package com.example.project_map.ui.other.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_map.data.repository.user.UserAuthRepository
import com.example.project_map.data.repository.user.UserAuthRepository.LoginResult // Import the sealed class
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    // Initialize the repository
    private val repository = UserAuthRepository()

    private val _navigationState = MutableLiveData<SplashNavigation?>()
    val navigationState: LiveData<SplashNavigation?> = _navigationState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun checkUserSession() {
        _isLoading.value = true

        viewModelScope.launch {
            // Call the NEW function we just added
            val result = repository.checkSession()

            _isLoading.value = false

            when (result) {
                is LoginResult.SuccessAdmin -> {
                    _navigationState.value = SplashNavigation.ToAdmin
                }
                is LoginResult.SuccessUser -> {
                    _navigationState.value = SplashNavigation.ToHome
                }
                is LoginResult.Error -> {
                    // Any error or "no session" means we go to Login
                    _navigationState.value = SplashNavigation.ToLogin
                }
            }
        }
    }

    fun onNavigationComplete() {
        _navigationState.value = null
    }
}
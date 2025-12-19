package com.example.project_map.ui.user.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_map.data.model.UserData
import com.example.project_map.data.repository.user.ProfileRepository
import kotlinx.coroutines.launch

class UserProfileViewModel : ViewModel() {

    private val repository = ProfileRepository()

    private val _userProfileState = MutableLiveData<ProfileState>()
    val userProfileState: LiveData<ProfileState> = _userProfileState

    private val _uploadState = MutableLiveData<UploadState>()
    val uploadState: LiveData<UploadState> = _uploadState

    // --- Function 1: Load User ---
    fun loadUserProfile(userId: String) {
        _userProfileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                val user = repository.getUserProfile(userId)
                _userProfileState.value = ProfileState.Success(user)
            } catch (e: Exception) {
                _userProfileState.value = ProfileState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

    // --- Function 2: Upload Image (Updated for Firebase Storage) ---
    fun uploadProfilePicture(userId: String, imageUri: Uri) {
        _uploadState.value = UploadState.Loading
        viewModelScope.launch {
            try {
                // 1. Upload to Firebase Storage
                // (Matches the function name used in the ProfileRepository refactor)
                val downloadUrl = repository.uploadImageToFirebase(userId, imageUri)

                // 2. Update Firestore
                repository.updateAvatarUrl(userId, downloadUrl)

                // 3. Notify Upload Success
                _uploadState.value = UploadState.Success(downloadUrl)

                // 4. Update local state
                val currentUser = (_userProfileState.value as? ProfileState.Success)?.user
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(avatarUrl = downloadUrl)
                    _userProfileState.value = ProfileState.Success(updatedUser)
                }

            } catch (e: Exception) {
                _uploadState.value = UploadState.Error(e.message ?: "Upload failed")
            }
        }
    }

    // --- Function 3: Update Text Data ---
    fun updateUserProfile(uid: String, name: String, email: String, phone: String) {
        _uploadState.value = UploadState.Loading

        viewModelScope.launch {
            try {
                repository.updateProfileData(uid, name, email, phone)
                loadUserProfile(uid) // Refresh data
                _uploadState.value = UploadState.Success("") // Signal success
            } catch (e: Exception) {
                _uploadState.value = UploadState.Error(e.message ?: "Update failed")
            }
        }
    }

    fun logout() {
        repository.logout()
    }

    // --- Sealed Classes ---
    sealed class ProfileState {
        object Loading : ProfileState()
        data class Success(val user: UserData) : ProfileState()
        data class Error(val message: String) : ProfileState()
    }

    sealed class UploadState {
        object Idle : UploadState()
        object Loading : UploadState()
        data class Success(val imageUrl: String) : UploadState()
        data class Error(val message: String) : UploadState()
    }
}
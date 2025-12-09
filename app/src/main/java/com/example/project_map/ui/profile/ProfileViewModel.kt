package com.example.project_map.ui.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_map.data.UserData
import com.example.project_map.data.repository.ProfileRepository
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    // MVVM Rule: No Firestore here. Only Repository.
    private val repository = ProfileRepository()

    // --- State 1: Reading User Data ---
    private val _userProfileState = MutableLiveData<ProfileState>()
    val userProfileState: LiveData<ProfileState> = _userProfileState

    // --- State 2: Uploading Image or Updating Data ---
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

    // --- Function 2: Upload Image ---
    fun uploadProfilePicture(userId: String, imageUri: Uri) {
        _uploadState.value = UploadState.Loading
        viewModelScope.launch {
            try {
                // 1. Upload to Cloudinary
                val downloadUrl = repository.uploadImageToCloudinary(userId, imageUri)

                // 2. Update Firestore (via Repository)
                repository.updateAvatarUrl(userId, downloadUrl)

                // 3. Notify Upload Success (This updates the image view)
                _uploadState.value = UploadState.Success(downloadUrl)

                // 4. FIX: DO NOT CALL loadUserProfile(userId) HERE.
                // It risks fetching old data.
                // Instead, just update the current profile state locally if needed.
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

    // --- Function 3: Update Text Data (Name, Email, Phone) ---
    fun updateUserProfile(uid: String, name: String, email: String, phone: String) {
        // Reuse Loading state (optional, or make a new UpdateState)
        _uploadState.value = UploadState.Loading

        viewModelScope.launch {
            try {
                // 1. Call Repository to update Firestore
                repository.updateProfileData(uid, name, email, phone)

                // 2. Refresh data on screen
                loadUserProfile(uid)

                // 3. Notify UI of success (reusing UploadState.Success or creating a new one)
                // Passing an empty string since there's no new image URL,
                // but this signals "Success" to the observer.
                _uploadState.value = UploadState.Success("")

            } catch (e: Exception) {
                _uploadState.value = UploadState.Error(e.message ?: "Update failed")
            }
        }
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
package com.example.project_map.ui.admin.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.project_map.data.model.UserData
import com.example.project_map.data.repository.admin.AdminUserRepository
import com.google.firebase.firestore.ListenerRegistration

class AdminUserViewModel : ViewModel() {

    private val repository = AdminUserRepository() // Panggil Repository

    private val _members = MutableLiveData<List<UserData>>()
    val members: LiveData<List<UserData>> = _members

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    private var snapshotListener: ListenerRegistration? = null

    init {
        loadMembers()
    }

    fun loadMembers() {
        // Delegasikan ke repository
        snapshotListener = repository.getMembersRealtime(
            onData = { list -> _members.value = list },
            onError = { errorMsg -> _message.value = errorMsg }
        )
    }

    fun updateMemberComplete(id: String, name: String, pokok: Double, wajib: Double) {
        repository.updateMember(id, name, pokok, wajib,
            onSuccess = { _message.value = "Data anggota berhasil diperbarui" },
            onFailure = { e -> _message.value = "Gagal update: ${e.message}" }
        )
    }

    override fun onCleared() {
        super.onCleared()
        snapshotListener?.remove()
    }
}
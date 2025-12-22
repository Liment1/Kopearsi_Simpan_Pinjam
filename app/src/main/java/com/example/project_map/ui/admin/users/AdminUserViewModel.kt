package com.example.project_map.ui.admin.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.project_map.data.model.UserData
import com.google.firebase.firestore.FirebaseFirestore

class AdminUserViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _members = MutableLiveData<List<UserData>>()
    val members: LiveData<List<UserData>> = _members

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    init {
        loadMembers()
    }

    fun loadMembers() {
        db.collection("users")
            .whereEqualTo("admin", false)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    _message.value = "Gagal memuat data: ${error.message}"
                    return@addSnapshotListener
                }

                val list = ArrayList<UserData>()
                for (doc in value!!) {
                    val user = doc.toObject(UserData::class.java)
                    user.id = doc.id
                    list.add(user)
                }
                _members.value = list
            }
    }

    // --- UPDATED: Removed 'score' parameter. It is now read-only. ---
    fun updateMemberComplete(id: String, name: String, pokok: Double, wajib: Double) {
        val updates = mapOf(
            "name" to name,
            // "creditScore" to score,  <-- REMOVED
            "simpananPokok" to pokok,
            "simpananWajib" to wajib
        )

        db.collection("users").document(id)
            .update(updates)
            .addOnSuccessListener {
                _message.value = "Data anggota berhasil diperbarui"
            }
            .addOnFailureListener { e ->
                _message.value = "Gagal update: ${e.message}"
            }
    }
}
package com.example.project_map.data.repository.admin

import com.example.project_map.data.model.Savings
import com.example.project_map.data.model.UserData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AdminSavingsRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getTransactionDetail(id: String, userId: String): Result<Savings> {
        return try {
            // 1. Look in the User's Savings History (Subcollection)
            val historyRef = db.collection("users")
                .document(userId)
                .collection("savings")
                .document(id)

            val historyDoc = historyRef.get().await()

            if (historyDoc.exists()) {
                val item = historyDoc.toObject(Savings::class.java)?.apply { this.id = historyDoc.id }
                if (item != null) return Result.success(item)
            }

            Result.failure(Exception("Data not found in History or Requests"))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserDetail(userId: String): Result<UserData> {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            val user = doc.toObject(UserData::class.java)
            if (user != null) Result.success(user) else Result.failure(Exception("User not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
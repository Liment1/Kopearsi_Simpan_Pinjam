package com.example.project_map.data.repository.admin

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AdminSettingsRepository {

    private val db = FirebaseFirestore.getInstance()

    // Fetch current settings
    suspend fun getGlobalSettings(): Map<String, Double> {
        val snapshot = db.collection("app_config").document("global_settings").get().await()
        if (snapshot.exists()) {
            return mapOf(
                "defaultInterest" to (snapshot.getDouble("defaultInterest") ?: 0.0),
                "lateFinePercentage" to (snapshot.getDouble("lateFinePercentage") ?: 0.0)
            )
        }
        // Return defaults if not found
        return mapOf("defaultInterest" to 0.0, "lateFinePercentage" to 0.0)
    }

    // Save settings (Update, fallback to Set if document missing)
    suspend fun updateGlobalSettings(updates: Map<String, Any>) {
        val docRef = db.collection("app_config").document("global_settings")
        try {
            docRef.update(updates).await()
        } catch (e: Exception) {
            docRef.set(updates).await()
        }
    }
}
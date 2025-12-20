package com.example.project_map.data.repository.user

import com.example.project_map.data.model.Notification
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.jvm.java

class UserNotificationRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getNotifications(): Flow<List<Notification>> = callbackFlow {
        // Currently fetching global announcements
        // TODO: In the future, you can merge this with db.collection("users/{uid}/notifications")
        val listener = db.collection("announcements")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = value?.toObjects(Notification::class.java) ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun markNotificationsAsRead() {
        val uid = auth.currentUser?.uid ?: return
        try {
            // Update the user's profile with the current time
            db.collection("users").document(uid)
                .update("lastReadAnnouncementDate", Timestamp.now())
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
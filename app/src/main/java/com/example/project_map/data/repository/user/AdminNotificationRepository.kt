package com.example.project_map.data.repository.admin

import com.example.project_map.data.model.Announcement
import com.example.project_map.data.model.ProfitDistributionRecord
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

class AdminNotificationRepository {

    private val db = FirebaseFirestore.getInstance()

    // 1. Get Announcement History (Real-time)
    fun getAnnouncementHistory(): Flow<List<Announcement>> = callbackFlow {
        val listener = db.collection("announcements")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = value?.toObjects(Announcement::class.java) ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // 2. Send Manual Notification
    suspend fun createAnnouncement(title: String, message: String, isUrgent: Boolean): Result<Unit> {
        return try {
            val announcement = Announcement(
                id = UUID.randomUUID().toString(),
                title = title,
                message = message,
                date = Timestamp.now(),
                isUrgent = isUrgent
            )
            db.collection("announcements").document(announcement.id).set(announcement).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. Complex Profit Distribution Logic
    suspend fun distributeProfit(): Result<String> {
        return try {
            // A. Get Latest Financial Report
            val reportSnapshot = db.collection("financial_reports")
                .orderBy("generatedAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            if (reportSnapshot.isEmpty) return Result.failure(Exception("Tidak ada laporan keuangan ditemukan"))

            val reportDoc = reportSnapshot.documents[0]
            val netProfit = reportDoc.getDouble("netProfit") ?: 0.0
            val totalRevenue = reportDoc.getDouble("totalRevenue") ?: 0.0
            val monthName = reportDoc.getString("month") ?: "Bulan Ini"

            if (netProfit <= 0) return Result.failure(Exception("Net Profit 0 atau negatif, tidak bisa membagikan."))

            // B. Get All Users
            val userSnapshot = db.collection("users").get().await()
            val users = userSnapshot.documents
            val totalMembers = users.size

            if (totalMembers == 0) return Result.failure(Exception("Tidak ada anggota aktif."))

            // C. Calculations
            val totalToDistribute = netProfit * 0.90
            val sharePerMember = totalToDistribute / totalMembers

            // D. Batch Write (Atomic Transaction)
            val batch = db.batch()

            // D1. Update Balance for all users
            for (userDoc in users) {
                batch.update(userDoc.reference, "totalSimpanan", FieldValue.increment(sharePerMember))
            }

            // D2. Record History
            val historyId = UUID.randomUUID().toString()
            val historyRef = db.collection("financial_reports")
                .document(reportDoc.id)
                .collection("distribution_history")
                .document(historyId)

            val record = ProfitDistributionRecord(
                id = historyId,
                sourceReportId = reportDoc.id,
                sourceMonth = monthName,
                totalRevenue = totalRevenue,
                netProfit = netProfit,
                distributedAmount = totalToDistribute,
                sharePerMember = sharePerMember,
                totalMembers = totalMembers,
                distributedAt = Timestamp.now()
            )
            batch.set(historyRef, record)

            // D3. Create Announcement
            val announcementId = UUID.randomUUID().toString()
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            val formattedShare = currencyFormat.format(sharePerMember)

            val announcement = Announcement(
                id = announcementId,
                title = "Pembagian SHU ($monthName)",
                message = "SHU sebesar 90% dari profit $monthName telah dibagikan. Anda menerima $formattedShare.",
                date = Timestamp.now(),
                isUrgent = true
            )
            batch.set(db.collection("announcements").document(announcementId), announcement)

            // E. Commit
            batch.commit().await()
            Result.success("Sukses! Profit $monthName dibagikan.")

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
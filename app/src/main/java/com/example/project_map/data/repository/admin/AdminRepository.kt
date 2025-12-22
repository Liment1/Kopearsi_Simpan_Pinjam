package com.example.project_map.data.repository.admin

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

class AdminRepository {

    private val db = FirebaseFirestore.getInstance()

    // --- FEATURE 1: Admin Sets Simpanan Pokok & Wajib ---
    suspend fun updateUserSavingsTypes(userId: String, pokok: Double, wajib: Double) {
        val updates = mapOf(
            "simpananPokok" to pokok,
            "simpananWajib" to wajib
            // Note: This updates the types specifically.
            // 'totalSimpanan' should ideally be recalculated: pokok + wajib + sukarela
        )
        db.collection("users").document(userId).update(updates).await()
    }

    // --- FEATURE 2: Process Withdrawal (The "Mega" Transaction) ---
    // This runs EVERYTHING automatically when Admin clicks "Approve"
    suspend fun approveWithdrawal(requestId: String, userId: String, amount: Double) {
        val requestRef = db.collection("withdrawal_requests").document(requestId)
        val userRef = db.collection("users").document(userId)

        db.runTransaction { transaction ->
            // 1. Mark Request as Approved
            transaction.update(requestRef, "status", "Disetujui")

            // 2. Deduct User's Balance (Simpanan Sukarela)
            transaction.update(userRef, "simpananSukarela", FieldValue.increment(-amount))
            transaction.update(userRef, "totalSimpanan", FieldValue.increment(-amount))

            // 3. Create Transaction History Log
            val historyRef = userRef.collection("savings").document()
            val historyData = hashMapOf(
                "amount" to amount,
                "type" to "Penarikan Disetujui",
                "date" to Date(),
                "status" to "Selesai"
            )
            transaction.set(historyRef, historyData)

            // 4. Send Notification (Database Trigger)
            val notifRef = db.collection("users").document(userId).collection("notifications").document()
            val notifData = hashMapOf(
                "title" to "Penarikan Berhasil",
                "message" to "Dana sebesar Rp ${amount.toInt()} telah disetujui dan ditransfer.",
                "date" to Date(),
                "isRead" to false
            )
            transaction.set(notifRef, notifData)

            // 5. Create Announcement (Global)
            val announceRef = db.collection("announcements").document()
            val announceData = hashMapOf(
                "title" to "Laporan Keuangan",
                "content" to "Telah dilakukan pencairan dana anggota sebesar Rp ${amount.toInt()}.",
                "date" to Date()
            )
            transaction.set(announceRef, announceData)

        }.await()
    }
}
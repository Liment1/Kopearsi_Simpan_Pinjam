package com.example.project_map.data.repository.user

import android.net.Uri
import android.util.Log
import com.example.project_map.data.CreditScoreManager
import com.example.project_map.data.model.Installment
import com.example.project_map.data.model.Loan
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage // Import Firebase Storage
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserLoanRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance() // Initialize Storage

    // 1. Fetch Credit Score
    suspend fun getCreditScore(userId: String): Double? {
        return suspendCoroutine { continuation ->
            CreditScoreManager.getScoreFromApi(userId) { score, _ ->
                continuation.resume(score)
            }
        }
    }

    // 2. Create Loan & Generate Installments
    suspend fun createLoan(userId: String, userName: String, loan: Loan, durationMonths: Int) {
        val batch = db.batch()

        val loanRef = db.collection("users").document(userId).collection("loans").document()
        val finalLoan = loan.copy(id = loanRef.id, namaPeminjam = userName)
        batch.set(loanRef, finalLoan)

        val calendar = Calendar.getInstance()
        val monthlyAmount = loan.sisaAngsuran / durationMonths

        for (i in 1..durationMonths) {
            calendar.add(Calendar.MONTH, 1)
            val instRef = loanRef.collection("installments").document()

            val installment = Installment(
                id = instRef.id,
                loanId = loanRef.id,
                bulanKe = i,
                jumlahBayar = monthlyAmount,
                jatuhTempo = calendar.time,
                status = "Belum Bayar"
            )
            batch.set(instRef, installment)
        }
        batch.commit().await()
    }

    // 3. Fetch Installments
    suspend fun getInstallments(userId: String, loanId: String): List<Installment> {
        Log.d("DEBUG_REPO", "Fetching installments for: $loanId")

        val snapshot = db.collection("users").document(userId)
            .collection("loans").document(loanId)
            .collection("installments")
            .orderBy("bulanKe", Query.Direction.ASCENDING)
            .get()
            .await()

        Log.d("DEBUG_REPO", "Found ${snapshot.size()} items")
        return snapshot.toObjects(Installment::class.java)
    }

    // 4. Pay Installment (Firebase Storage + Firestore Transaction)
    suspend fun payInstallment(userId: String, installment: Installment, imageUri: Uri) {
        // A. Upload to Firebase Storage
        // Passing loanId to keep folder structure organized
        val proofUrl = uploadProofToFirebase(userId, installment.loanId, imageUri)

        // B. Update Firestore
        val loanRef = db.collection("users").document(userId).collection("loans").document(installment.loanId)
        val instRef = loanRef.collection("installments").document(installment.id)

        db.runTransaction { transaction ->
            transaction.update(instRef, mapOf(
                "status" to "Menunggu Konfirmasi",
                "tanggalBayar" to Date(),
                "buktiBayarUrl" to proofUrl
            ))

            // Optional: You might not want to update totals until confirmed,
            // but keeping logic consistent with your original code:
            transaction.update(loanRef, "sisaAngsuran", FieldValue.increment(-installment.jumlahBayar))
            transaction.update(loanRef, "totalDibayar", FieldValue.increment(installment.jumlahBayar))
        }.await()
    }

    // 5. Pay Installment via Balance
    suspend fun payInstallmentViaBalance(userId: String, installment: Installment) {
        val userRef = db.collection("users").document(userId)
        val loanRef = userRef.collection("loans").document(installment.loanId)
        val instRef = loanRef.collection("installments").document(installment.id)

        db.runTransaction { transaction ->
            val userSnapshot = transaction.get(userRef)
            val currentBalance = userSnapshot.getDouble("totalSimpanan") ?: 0.0

            if (currentBalance < installment.jumlahBayar) {
                throw FirebaseFirestoreException(
                    "Saldo tidak mencukupi!",
                    FirebaseFirestoreException.Code.ABORTED
                )
            }

            val newBalance = currentBalance - installment.jumlahBayar
            transaction.update(userRef, "totalSimpanan", newBalance)

            transaction.update(instRef, mapOf(
                "status" to "Lunas",
                "tanggalBayar" to Date(),
                "buktiBayarUrl" to "Potong Saldo"
            ))

            transaction.update(loanRef, "sisaAngsuran", FieldValue.increment(-installment.jumlahBayar))
            transaction.update(loanRef, "totalDibayar", FieldValue.increment(installment.jumlahBayar))

            val transRef = userRef.collection("savings").document()
            val transData = hashMapOf(
                "amount" to installment.jumlahBayar,
                "date" to Date(),
                "type" to "Pembayaran Angsuran Ke-${installment.bulanKe}",
                "status" to "Berhasil",
                "userId" to userId
            )
            transaction.set(transRef, transData)

        }.await()
    }

    suspend fun getLoan(userId: String, loanId: String): Loan? {
        val snapshot = db.collection("users").document(userId)
            .collection("loans").document(loanId)
            .get()
            .await()
        return snapshot.toObject(Loan::class.java)
    }

    // --- REPLACED CLOUDINARY WITH FIREBASE STORAGE ---
    private suspend fun uploadProofToFirebase(userId: String, loanId: String, uri: Uri): String {
        return try {
            val filename = "${UUID.randomUUID()}.jpg"
            // Directory: installments/USER_ID/LOAN_ID/FILENAME
            val ref = storage.reference.child("installments/$userId/$loanId/$filename")

            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw Exception("Upload failed: ${e.message}")
        }
    }
}
package com.example.project_map.data.repository.user

import android.net.Uri
import com.example.project_map.data.CreditScoreManager
import com.example.project_map.data.model.Installment
import com.example.project_map.data.model.Loan
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserLoanRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // ... createLoan, payInstallment, payInstallmentViaBalance remain the same ...
    // (I am keeping the other functions hidden for brevity, but they exist as before)

    suspend fun createLoan(userId: String, userName: String, loan: Loan, durationMonths: Int, ktpUrl: Uri) {
        val batch = db.batch()
        val loanRef = db.collection("users").document(userId).collection("loans").document()

        val finalLoan = loan.copy(
            id = loanRef.id,
            userId = userId,
            namaPeminjam = userName,
            tanggalPengajuan = Date()
        )
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
                status = "Belum Bayar",
            )
            batch.set(instRef, installment)
        }
        batch.commit().await()
    }

    suspend fun payInstallment(userId: String, installment: Installment, imageUri: Uri) {
        val proofUrl = uploadProofToFirebase(userId, installment.loanId, imageUri)

        val loanRef = db.collection("users").document(userId).collection("loans").document(installment.loanId)
        val instRef = loanRef.collection("installments").document(installment.id)

        db.runTransaction { transaction ->
            transaction.update(instRef, mapOf(
                "status" to "Lunas",
                "tanggalBayar" to Date(),
                "buktiBayarUrl" to proofUrl
            ))
            transaction.update(loanRef, "sisaAngsuran", FieldValue.increment(-installment.jumlahBayar))
            transaction.update(loanRef, "totalDibayar", FieldValue.increment(installment.jumlahBayar))
        }.await()
    }

    suspend fun payInstallmentViaBalance(userId: String, installment: Installment) {
        val userRef = db.collection("users").document(userId)
        val loanRef = userRef.collection("loans").document(installment.loanId)
        val instRef = loanRef.collection("installments").document(installment.id)

        db.runTransaction { transaction ->
            val userSnapshot = transaction.get(userRef)
            val currentBalance = userSnapshot.getDouble("totalSimpanan") ?: 0.0

            if (currentBalance < installment.jumlahBayar) {
                throw FirebaseFirestoreException("Saldo tidak mencukupi!", FirebaseFirestoreException.Code.ABORTED)
            }

            transaction.update(userRef, "totalSimpanan", currentBalance - installment.jumlahBayar)
            transaction.update(instRef, mapOf(
                "status" to "Lunas",
                "tanggalBayar" to Date(),
                "buktiBayarUrl" to "Potong Saldo"
            ))
            transaction.update(loanRef, "sisaAngsuran", FieldValue.increment(-installment.jumlahBayar))
            transaction.update(loanRef, "totalDibayar", FieldValue.increment(installment.jumlahBayar))
        }.await()
    }

    suspend fun getInstallments(userId: String, loanId: String): List<Installment> {
        val snapshot = db.collection("users").document(userId)
            .collection("loans").document(loanId)
            .collection("installments")
            .orderBy("bulanKe", Query.Direction.ASCENDING)
            .get()
            .await()
        return snapshot.toObjects(Installment::class.java)
    }

    suspend fun getLoan(userId: String, loanId: String): Loan? {
        val snapshot = db.collection("users").document(userId)
            .collection("loans").document(loanId)
            .get()
            .await()
        return snapshot.toObject(Loan::class.java)
    }

    fun getActiveLoansStream(userId: String, onUpdate: (List<Loan>) -> Unit): com.google.firebase.firestore.ListenerRegistration {
        return db.collection("users").document(userId).collection("loans")
            .whereIn("status", listOf("Proses", "Disetujui", "Berjalan", "Ditolak", "Lunas"))
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                val list = snapshots?.toObjects(Loan::class.java) ?: emptyList()
                onUpdate(list)
            }
    }

    suspend fun getCreditScore(userId: String): Double? {
        return suspendCoroutine { continuation ->
            CreditScoreManager.getScoreFromApi(userId) { score, _ -> continuation.resume(score) }
        }
    }

    private suspend fun uploadProofToFirebase(userId: String, loanId: String, uri: Uri): String {
        val filename = "${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child("installments/$userId/$loanId/$filename")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}
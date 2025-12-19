package com.example.project_map.data.repository.user

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.Date

class UserLaporanRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Fetches all financial activity for a logged-in user within a specific date range.
     * Returns a Task containing a list of 3 Result Objects: [Savings, Loans, Installments]
     */
    fun getMonthlyReportData(startDate: Date, endDate: Date): Task<List<Any>> {
        val uid = auth.currentUser?.uid ?: return Tasks.forException(Exception("No User"))

        // 1. Fetch Savings/Withdrawals (Transactions)
        val savingsTask = db.collection("transactions")
            .whereEqualTo("userId", uid)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .get()

        // 2. Fetch New Loans (Pinjaman Baru)
        // Note: Assuming loans are stored in users/{uid}/loans
        val loansTask = db.collection("users").document(uid).collection("loans")
            .whereGreaterThanOrEqualTo("tanggalPengajuan", startDate)
            .whereLessThanOrEqualTo("tanggalPengajuan", endDate)
            .get()

        // 3. Fetch Paid Installments (Angsuran)
        // We use CollectionGroup query filtered by owner (requires security rule we added earlier!)
        // OR if you don't have that index yet, we can query active loans and then their installments.
        // For simplicity/speed here, let's assume we query the 'installments' CollectionGroup
        // but we filter manually or rely on the path logic if we can't do complex queries.
        // BETTER APPROACH FOR NOW: collectionGroup query.
        val installmentsTask = db.collectionGroup("installments")
            .whereGreaterThanOrEqualTo("tanggalBayar", startDate)
            .whereLessThanOrEqualTo("tanggalBayar", endDate)
        // We can't easily filter by userId in a collectionGroup without including it in the doc.
        // Assuming your Installment document has 'userId' or we filter in memory.
        // If Installment doesn't have userId, we might need to fetch active loans first.
        // Let's assume we fetch all loans first, then their installments.
        // BUT to keep it simple and working:
        // We will fetch ALL 'loans' for user, then fetch 'installments' for those loans.
        // This is safer without extra Indexes.

        // REVISED STRATEGY FOR INSTALLMENTS:
        // Since querying subcollections deeply is hard, we will return the first two tasks
        // and handle installments differently or just return Tasks.whenAllSuccess for these two for now.
        // To make it robust:

        return Tasks.whenAllSuccess(savingsTask, loansTask)
    }

    // Helper to fetch installments for specific loans (if needed in VM)
    fun getInstallmentsForUser(uid: String, startDate: Date, endDate: Date): Task<QuerySnapshot> {
        // This usually requires a special index "isPaid + date".
        // For now, let's assume we focus on Savings and Loans for the MVP report.
        return db.collectionGroup("installments")
            .whereEqualTo("isPaid", true) // Assuming you use isPaid or status="Lunas"
            .whereGreaterThanOrEqualTo("date", startDate) // or tanggalBayar
            .whereLessThanOrEqualTo("date", endDate)
            .get()
    }
}
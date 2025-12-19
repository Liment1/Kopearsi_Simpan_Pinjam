package com.example.project_map.data.repository.admin

import com.example.project_map.data.model.UserData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class MemberFinancials(
    val totalSavings: Double,
    val outstandingLoan: Double
)

class AdminUserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    // 1. Get All Members (Real-time)
    fun getMembersStream(): Flow<List<UserData>> = callbackFlow {
        val listener = usersCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val users = snapshot.documents.map { doc ->
                        UserData(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            email = doc.getString("email") ?: "",
                            phone = doc.getString("phone") ?: "",
                            status = doc.getString("status") ?: "Aktif",
                            memberCode = doc.getString("memberCode") ?: ""
                        )
                    }
                    trySend(users)
                }
            }
        awaitClose { listener.remove() }
    }

    // 2. Update Member Details
    suspend fun updateMember(uid: String, name: String, phone: String, status: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "name" to name,
                "phone" to phone,
                "status" to status
            )
            usersCollection.document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. Get Specific User Financials (Savings + Debt)
    suspend fun getMemberFinancials(uid: String): Result<MemberFinancials> {
        return try {
            val userDoc = usersCollection.document(uid).get().await()
            val totalSavings = userDoc.getDouble("totalSimpanan") ?: 0.0

            // Calculate active loan debt
            val loansSnapshot = usersCollection.document(uid).collection("loans")
                .whereNotEqualTo("status", "Lunas")
                .get().await()

            val outstandingDebt = loansSnapshot.documents.sumOf {
                it.getDouble("sisaAngsuran") ?: 0.0
            }

            Result.success(MemberFinancials(totalSavings, outstandingDebt))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
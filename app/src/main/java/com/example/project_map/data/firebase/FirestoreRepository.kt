import com.google.firebase.firestore.FirebaseFirestore
import com.example.project_map.data.Loan // Your data class

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val loansCollection = db.collection("loans")

    // Add a new loan
    fun addLoan(loan: Loan, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        // You might need to adapt your Loan data class to be Firestore-friendly
        // or map it to a HashMap
        loansCollection.add(loan)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    // Get loans for a specific user
    fun getLoansForUser(userId: String, onResult: (List<Loan>) -> Unit) {
        loansCollection.whereEqualTo("userId", userId) // Assuming you add userId to Loan class
            .get()
            .addOnSuccessListener { result ->
                val loanList = result.toObjects(Loan::class.java)
                onResult(loanList)
            }
            .addOnFailureListener {
                onResult(emptyList()) // Handle error appropriately
            }
    }
}
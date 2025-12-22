package com.example.project_map.data

import android.util.Log
import com.example.project_map.api.ApiClient
import com.example.project_map.api.CreditScoreResponse
import com.example.project_map.api.ScoreRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object CreditScoreManager {

    private val db = FirebaseFirestore.getInstance()

    fun getScoreFromApi(userId: String, callback: (Double?, String?) -> Unit) {

        // 1. Fetch Real Data
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val realLimitBal = document.getDouble("totalSimpanan") ?: 50000.0
                val realBillAmt = document.getDouble("simpananWajib") ?: 20000.0
                val realPayAmt = document.getDouble("simpananSukarela") ?: 0.0

                val request = ScoreRequest(
                    userId = userId,
                    limitBal = realLimitBal,
                    sex = 2, education = 2, marriage = 1, age = 35,
                    pay1 = 0, pay2 = 0, pay3 = 0, pay4 = 0, pay5 = 0, pay6 = 0,
                    billAmt1 = realBillAmt, billAmt2 = realBillAmt, billAmt3 = realBillAmt,
                    billAmt4 = 0.0, billAmt5 = 0.0, billAmt6 = 0.0,
                    payAmt1 = realPayAmt, payAmt2 = realPayAmt, payAmt3 = 1000.0,
                    payAmt4 = 1000.0, payAmt5 = 1000.0, payAmt6 = 1000.0
                )

                performApiCall(userId, request, callback)
            }
            .addOnFailureListener {
                // Fallback Defaults
                val defaultRequest = ScoreRequest(userId = userId, limitBal = 50000.0, /*... defaults ...*/
                    sex=2, education=2, marriage=1, age=35, pay1=0, pay2=0, pay3=0, pay4=0, pay5=0, pay6=0,
                    billAmt1=20000.0, billAmt2=20000.0, billAmt3=20000.0, billAmt4=0.0, billAmt5=0.0, billAmt6=0.0,
                    payAmt1=0.0, payAmt2=0.0, payAmt3=0.0, payAmt4=0.0, payAmt5=0.0, payAmt6=0.0)
                performApiCall(userId, defaultRequest, callback)
            }
    }

    private fun performApiCall(userId: String, request: ScoreRequest, callback: (Double?, String?) -> Unit) {
        val call = ApiClient.apiService.getCreditScore(request)

        call.enqueue(object : Callback<CreditScoreResponse> {
            override fun onResponse(call: Call<CreditScoreResponse>, response: Response<CreditScoreResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    // --- NEW: Save to Firestore Immediately ---
                    val updateData = mapOf(
                        "creditScore" to body.score,
                        "creditDecision" to body.keputusan
                    )
                    // Use SetOptions.merge to avoid overwriting other user fields
                    db.collection("users").document(userId)
                        .set(updateData, SetOptions.merge())

                    // Proceed with UI callback
                    callback(body.score, body.keputusan)
                } else {
                    Log.e("API_SCORE", "Error: ${response.code()}")
                    callback(null, null)
                }
            }

            override fun onFailure(call: Call<CreditScoreResponse>, t: Throwable) {
                Log.e("API_SCORE", "Failure: ${t.message}")
                callback(null, null)
            }
        })
    }
}
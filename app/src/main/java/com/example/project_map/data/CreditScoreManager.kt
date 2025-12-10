package com.example.project_map.data

import android.util.Log
import com.example.project_map.api.ApiClient
import com.example.project_map.api.CreditScoreResponse
import com.example.project_map.api.ScoreRequest

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object CreditScoreManager {

    // Helper to get parameters (Historical or Default)
    private fun getCalculationParameters(userId: String): ScoreRequest {
        // TODO: Try to fetch real history from Firestore here.
        // val history = db.collection("transactions")...
        // If history exists, calculate metrics.

        // IF NO HISTORY, USE DEFAULTS:
        return ScoreRequest(
            userId = userId,
            limitBal = 50000,
            sex = 2,
            education = 2,
            marriage = 1,
            age = 35,
            pay1 = 2, pay2 = 2, pay3 = 2, pay4 = 0, pay5 = 0, pay6 = 0,
            billAmt1 = 40000.0, billAmt2 = 40000.0, billAmt3 = 40000.0,
            billAmt4 = 20000.0, billAmt5 = 20000.0, billAmt6 = 20000.0,
            payAmt1 = 0.0, payAmt2 = 0.0, payAmt3 = 1000.0,
            payAmt4 = 1000.0, payAmt5 = 1000.0, payAmt6 = 1000.0
        )
    }

    fun getScoreFromApi(userId: String, callback: (Double?, String?) -> Unit) {
        val request = getCalculationParameters(userId)
        val call = ApiClient.apiService.getCreditScore(request)

        call.enqueue(object : Callback<CreditScoreResponse> {
            override fun onResponse(call: Call<CreditScoreResponse>, response: Response<CreditScoreResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
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
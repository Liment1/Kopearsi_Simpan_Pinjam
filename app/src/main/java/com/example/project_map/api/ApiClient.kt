package com.example.project_map.api

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import retrofit2.http.Body
import com.google.gson.annotations.SerializedName

// 1. Update Request to match your exact default values
data class ScoreRequest(
    @SerializedName("user_id") val userId: String,

    @SerializedName("LIMIT_BAL") val limitBal: Double,
    @SerializedName("SEX") val sex: Int,
    @SerializedName("EDUCATION") val education: Int,
    @SerializedName("MARRIAGE") val marriage: Int,
    @SerializedName("AGE") val age: Int,

    @SerializedName("PAY_1") val pay1: Int,
    @SerializedName("PAY_2") val pay2: Int,
    @SerializedName("PAY_3") val pay3: Int,
    @SerializedName("PAY_4") val pay4: Int,
    @SerializedName("PAY_5") val pay5: Int,
    @SerializedName("PAY_6") val pay6: Int,

    @SerializedName("BILL_AMT1") val billAmt1: Double,
    @SerializedName("BILL_AMT2") val billAmt2: Double,
    @SerializedName("BILL_AMT3") val billAmt3: Double,
    @SerializedName("BILL_AMT4") val billAmt4: Double,
    @SerializedName("BILL_AMT5") val billAmt5: Double,
    @SerializedName("BILL_AMT6") val billAmt6: Double,

    @SerializedName("PAY_AMT1") val payAmt1: Double,
    @SerializedName("PAY_AMT2") val payAmt2: Double,
    @SerializedName("PAY_AMT3") val payAmt3: Double,
    @SerializedName("PAY_AMT4") val payAmt4: Double,
    @SerializedName("PAY_AMT5") val payAmt5: Double,
    @SerializedName("PAY_AMT6") val payAmt6: Double
)

// 2. Update Response to handle Double score (83.01)
data class CreditScoreResponse(
    val score: Double, // Changed from Int to Double
    val keputusan: String,
    val status: String
)

interface ApiService {
    @POST("credit_score")
    fun getCreditScore(@Body requestBody: ScoreRequest): Call<CreditScoreResponse>
}

object ApiClient {
    private val BASE_URL = "https://koperasi-ml-api-production.up.railway.app/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
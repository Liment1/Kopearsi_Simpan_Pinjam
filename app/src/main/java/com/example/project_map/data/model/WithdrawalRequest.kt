package com.example.project_map.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.util.Date

data class WithdrawalRequest(
    @DocumentId
    var id: String = "",
    val userId: String = "",

    var userName: String = "",

    val amount: Double = 0.0,
    val bankName: String = "",
    val accountNumber: String = "",
    val status: String = "",
    val date: Date? = null,

    @get:Exclude
    var userAvatarUrl: String = ""
)
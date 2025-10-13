package com.example.project_map.ui.savings

import com.example.app.savings.TransactionType

data class Transaction(
    val tanggal: String,
    val keterangan: String,
    val jumlah: String,
    val type: TransactionType,
    val imageUri: String? = null
)

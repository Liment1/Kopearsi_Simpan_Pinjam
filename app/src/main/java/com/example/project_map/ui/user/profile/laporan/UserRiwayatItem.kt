package com.example.project_map.ui.user.profile.laporan

import androidx.annotation.ColorInt

data class UserRiwayatItem(
    val date: String,
    val description: String,
    val amount: String,
    @ColorInt val color: Int
)
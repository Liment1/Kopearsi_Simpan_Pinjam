package com.example.project_map.ui.user.profile.report

import androidx.annotation.ColorInt

data class UserItemHistory(
    val date: String,
    val description: String,
    val amount: String,
    @ColorInt val color: Int
)
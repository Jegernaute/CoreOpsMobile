package com.example.coreops.data.remote.models

import com.google.gson.annotations.SerializedName

data class ChecklistDto(
    val id: Int,
    val content: String,
    @SerializedName("is_completed")
    val isCompleted: Boolean
)
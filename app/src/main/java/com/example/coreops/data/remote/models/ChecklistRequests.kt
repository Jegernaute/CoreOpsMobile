package com.example.coreops.data.remote.models

import com.google.gson.annotations.SerializedName

data class CreateChecklistItemRequest(
    val task: Int,
    val content: String
)

data class UpdateChecklistItemRequest(
    @SerializedName("is_completed")
    val isCompleted: Boolean
)
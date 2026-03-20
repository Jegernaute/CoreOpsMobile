package com.example.coreops.data.remote.models

import com.google.gson.annotations.SerializedName

/**
 * DTO для часткового оновлення (PATCH) задачі а саме статусу.
 */
data class TaskStatusUpdateRequest(
    @SerializedName("status")
    val status: String
)
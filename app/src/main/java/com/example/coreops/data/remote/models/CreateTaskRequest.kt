package com.example.coreops.data.remote.models

import com.google.gson.annotations.SerializedName

data class CreateTaskRequest(
    val title: String,
    val description: String?,
    val project: Int,
    @SerializedName("task_type")
    val taskType: String,
    val priority: String,
    val status: String = "to_do"
)
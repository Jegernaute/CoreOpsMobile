package com.example.coreops.data.remote.models

import com.google.gson.annotations.SerializedName

data class CreateTaskRequest(
    val title: String,
    val description: String?,
    val project: Int,
    @SerializedName("task_type")
    val taskType: String,
    val priority: String,
    val status: String,
    val assignee: Int? = null,

    @SerializedName("estimated_hours")
    val estimatedHours: Float? = null,

    @SerializedName("due_date")
    val dueDate: String? = null
)
package com.example.coreops.data.remote.models

import com.google.gson.annotations.SerializedName

data class SprintDto(
    val id: Int,
    val project: Int,
    val name: String,
    val goal: String?,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String,
    val status: String,
    @SerializedName("actual_end_date")
    val actualEndDate: String?,
    @SerializedName("tasks_total")
    val tasksTotal: Int,
    @SerializedName("tasks_completed")
    val tasksCompleted: Int,
    @SerializedName("created_at")
    val createdAt: String
)
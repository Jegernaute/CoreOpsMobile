package com.example.coreops.data.remote.models

import com.google.gson.annotations.SerializedName

// 1. Модель для отримання даних (Тут ми видалили зайвий "val task")
data class TaskDto(
    val id: Int,
    val title: String,
    val description: String?,
    val status: String,
    val priority: String,

    @SerializedName("task_type")
    val taskType: String,

    @SerializedName("assignee_name")
    val assigneeName: String?,

    @SerializedName("reporter_name")
    val reporterName: String,

    @SerializedName("project_name")
    val projectName: String,

    @SerializedName("estimated_hours")
    val estimatedHours: Float?,

    @SerializedName("due_date")
    val dueDate: String?,

    val sprint: Int? = null,

    @SerializedName("actual_hours")
    val actualHours: Float? = null,

    val comments: List<TaskCommentDto> = emptyList(),
    val resources: List<TaskResourceDto> = emptyList()
)
data class TaskCommentDto(
    val id: Int,
    @SerializedName("author_name")
    val authorName: String,
    val content: String,
    @SerializedName("created_at")
    val createdAt: String
)

data class TaskResourceDto(
    val id: Int,
    val name: String,
    @SerializedName("resource_type")
    val resourceType: String,
    val file: String?,
    val url: String?
)
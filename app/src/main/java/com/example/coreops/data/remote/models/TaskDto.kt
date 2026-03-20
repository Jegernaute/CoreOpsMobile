package com.example.coreops.data.remote.models

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object (DTO) для отримання задачі з сервера.
 */
data class TaskDto(
    val id: Int,
    val title: String,
    val description: String?,
    val status: String,
    val priority: String,

    // Використовується SerializedName, щоб зберегти Kotlin-стиль (camelCase) для змінних,
    // але правильно читати JSON від Django (snake_case)
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
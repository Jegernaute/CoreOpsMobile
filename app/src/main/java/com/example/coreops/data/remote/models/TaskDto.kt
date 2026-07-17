package com.example.coreops.data.remote.models

import com.google.gson.annotations.SerializedName

data class TaskDto(
    val id: Int,
    @SerializedName("task_key")
    val taskKey: String?,
    val title: String,
    val description: String?,
    val status: String,
    val priority: String,

    @SerializedName("task_type")
    val taskType: String,

    // Нові вкладені об'єкти
    @SerializedName("assignee_details")
    val assigneeDetails: UserDetailsDto?,

    @SerializedName("reporter_details")
    val reporterDetails: UserDetailsDto?,

    // Масиви
    val checklist: List<ChecklistDto> = emptyList(),
    val resources: List<ResourceDto> = emptyList(),

    // Старі поля для сумісності з іншими екранами (можна залишити)
    @SerializedName("assignee_name") val assigneeName: String?,
    @SerializedName("assignee_avatar") val assigneeAvatar: String?,
    @SerializedName("reporter_name") val reporterName: String?,
    @SerializedName("reporter_avatar") val reporterAvatar: String?,

    @SerializedName("project_name")
    val projectName: String,
    @SerializedName("project_key")
    val projectKey: String,

    @SerializedName("comments_count")
    val commentsCount: Int = 0,
    @SerializedName("resources_count")
    val resourcesCount: Int = 0,

    @SerializedName("estimated_hours")
    val estimatedHours: Float?,
    @SerializedName("due_date")
    val dueDate: String?,

    val sprint: Int? = null,
    @SerializedName("sprint_name")
    val sprintName: String? = null
)
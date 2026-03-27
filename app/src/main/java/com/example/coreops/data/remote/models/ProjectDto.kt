package com.example.coreops.data.remote.models

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object (DTO) для отримання даних проєкту з сервера.
 */
data class ProjectDto(
    val id: Int,
    val key: String,
    val name: String,
    val description: String?,
    val status: String,
    val members: List<ProjectMemberDto> = emptyList()
)

data class ProjectMemberDto(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_email")
    val userEmail: String,
    @SerializedName("user_name")
    val userName: String,
    val role: String,
    @SerializedName("joined_at")
    val joinedAt: String
)
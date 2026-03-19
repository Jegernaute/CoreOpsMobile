package com.example.coreops.data.remote.models

/**
 * Data Transfer Object (DTO) для отримання даних проєкту з сервера.
 */
data class ProjectDto(
    val id: Int,
    val key: String,
    val name: String,
    // Опис може бути порожнім
    val description: String?,
    val status: String
)
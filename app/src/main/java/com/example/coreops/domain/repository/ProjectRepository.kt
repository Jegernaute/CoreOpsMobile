package com.example.coreops.domain.repository

import com.example.coreops.data.remote.models.PaginatedResponse
import com.example.coreops.data.remote.models.ProjectDto

/**
 * Інтерфейс репозиторію проєктів.
 * ViewModel буде спілкуватися тільки з цим контрактом, не знаючи про Retrofit чи API.
 */
interface ProjectRepository {
    // Передає cursor і повертає повну пагіновану відповідь
    suspend fun getProjects(cursor: String? = null): Result<PaginatedResponse<ProjectDto>>
    suspend fun getProjectById(id: Int): Result<ProjectDto>
}
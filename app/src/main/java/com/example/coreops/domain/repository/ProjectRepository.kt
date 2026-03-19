package com.example.coreops.domain.repository

import com.example.coreops.data.remote.models.ProjectDto

/**
 * Інтерфейс репозиторію проєктів.
 * ViewModel буде спілкуватися тільки з цим контрактом, не знаючи про Retrofit чи API.
 */
interface ProjectRepository {
    suspend fun getProjects(): Result<List<ProjectDto>>
}
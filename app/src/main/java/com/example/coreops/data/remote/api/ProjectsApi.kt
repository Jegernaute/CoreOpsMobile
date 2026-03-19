package com.example.coreops.data.remote.api

import com.example.coreops.data.remote.models.PaginatedResponse
import com.example.coreops.data.remote.models.ProjectDto
import retrofit2.http.GET

/**
 * Інтерфейс для роботи з API проєктів.
 */
interface ProjectsApi {

    // Ендпоінт для отримання списку всіх проєктів
    @GET("api/v1/projects/")
    suspend fun getProjects(): PaginatedResponse<ProjectDto>

}
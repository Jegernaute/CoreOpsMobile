package com.example.coreops.data.remote.api

import com.example.coreops.data.remote.models.PaginatedResponse
import com.example.coreops.data.remote.models.ProjectDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Інтерфейс для роботи з API проєктів.
 */
interface ProjectsApi {

    // Ендпоінт для отримання списку всіх проєктів
    @GET("api/v1/projects/")
    suspend fun getProjects(@Query("cursor") cursor: String? = null): PaginatedResponse<ProjectDto>

    @GET("api/v1/projects/{id}/")
    suspend fun getProjectById(@Path("id") id: Int): ProjectDto

}
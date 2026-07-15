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
    suspend fun getProjects(
        @Query("cursor") cursor: String? = null,
        @Query("search") search: String? = null,
        @Query("ordering") ordering: String? = null,
        @Query("show_archived") showArchived: Boolean? = null,
        @Query("status") status: String? = null,
        @Query("has_active_tasks") hasActiveTasks: Boolean? = null,
        @Query("is_completed") isCompleted: Boolean? = null
    ): PaginatedResponse<ProjectDto>
    @GET("api/v1/projects/{id}/")
    suspend fun getProjectById(@Path("id") id: Int): ProjectDto

}
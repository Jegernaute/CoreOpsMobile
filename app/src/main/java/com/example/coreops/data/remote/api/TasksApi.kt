package com.example.coreops.data.remote.api

import com.example.coreops.data.remote.models.CommentDto
import com.example.coreops.data.remote.models.CommentRequest
import com.example.coreops.data.remote.models.PaginatedResponse
import com.example.coreops.data.remote.models.TaskDto
import com.example.coreops.data.remote.models.TaskStatusUpdateRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Інтерфейс для роботи з API задач.
 */
interface TasksApi {

    // Ендпоінт для отримання списку задач конкретного проєкту
    @GET("api/v1/tasks/")
    suspend fun getTasks(
        @Query("project") projectId: Int // Retrofit перетворить це на ?project=ID
    ): PaginatedResponse<TaskDto>

    /**
     * Отримання повної інформації про одну задачу за її ID (включаючи коментарі та ресурси).
     */
    @GET("api/v1/tasks/{id}/")
    suspend fun getTaskById(
        @Path("id") taskId: Int
    ): TaskDto

    /**
     * Часткове оновлення задачі.
     * Використовує Map, щоб передати лише ті поля, які реально змінилися.
     */
    @PATCH("api/v1/tasks/{id}/")
    suspend fun updateTaskStatus(
        @Path("id") taskId: Int,
        @Body request: TaskStatusUpdateRequest
    ): TaskDto

    /**
     * Отримання списку коментарів для конкретної задачі.
     * Django чекає: GET /api/v1/tasks/comments/?task={id}
     */
    @GET("api/v1/tasks/comments/")
    suspend fun getTaskComments(
        @Query("task") taskId: Int
    ): PaginatedResponse<CommentDto>

    /**
     * Створення нового коментаря.
     * Django чекає: POST /api/v1/tasks/comments/
     */
    @POST("api/v1/tasks/comments/")
    suspend fun addTaskComment(
        @Body request: CommentRequest
    ): CommentDto
}
package com.example.coreops.data.remote.api

import com.example.coreops.data.remote.models.ChecklistDto
import com.example.coreops.data.remote.models.CommentDto
import com.example.coreops.data.remote.models.CommentRequest
import com.example.coreops.data.remote.models.CreateChecklistItemRequest
import com.example.coreops.data.remote.models.CreateTaskRequest
import com.example.coreops.data.remote.models.HistoryEventDto
import com.example.coreops.data.remote.models.PaginatedResponse
import com.example.coreops.data.remote.models.SprintDto
import com.example.coreops.data.remote.models.TaskDto
import com.example.coreops.data.remote.models.TaskStatusUpdateRequest
import com.example.coreops.data.remote.models.UpdateChecklistItemRequest
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
        @Query("project") projectId: Int,
        @Query("priority") priority: String? = null,
        @Query("task_type") taskType: String? = null,
        @Query("reporter") reporter: Int? = null,
        @Query("assignee") assignee: Int? = null,
        @Query("due_date_after") dueDateAfter: String? = null,
        @Query("due_date_before") dueDateBefore: String? = null,
        @Query("due_date") dueDate: String? = null,
        @Query("cursor") cursor: String? = null
    ): PaginatedResponse<TaskDto>

    // Всі доступні задачі з підтримкою фільтрів та сортування
    @GET("api/v1/tasks/")
    suspend fun getAllMyTasks(
        @Query("project") project: Int? = null,
        @Query("priority") priority: String? = null,
        @Query("task_type") taskType: String? = null,
        @Query("reporter") reporter: Int? = null,
        @Query("assignee") assignee: Int? = null,
        @Query("status") status: String? = null,
        @Query("ordering") ordering: String? = null,
        @Query("search") search: String? = null,
        @Query("due_date_after") dueDateAfter: String? = null,
        @Query("due_date_before") dueDateBefore: String? = null,
        @Query("due_date") dueDate: String? = null,
        @Query("cursor") cursor: String? = null
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
        @Query("task") taskId: Int,
        @Query("cursor") cursor: String? = null
    ): PaginatedResponse<CommentDto>

    /**
     * Створення нового коментаря.
     * Django чекає: POST /api/v1/tasks/comments/
     */
    @POST("api/v1/tasks/comments/")
    suspend fun addTaskComment(
        @Body request: CommentRequest
    ): CommentDto

    /**
     * Створення нової задачі.
     */
    @POST("api/v1/tasks/")
    suspend fun createTask(
        @Body request: CreateTaskRequest
    ): TaskDto

    /**
     * Отримання активних спринтів проєкту.
     */
    @GET("api/v1/planning/")
    suspend fun getActiveSprints(
        @Query("project") projectId: Int,
        @Query("status") status: String
    ): PaginatedResponse<SprintDto>

    /**
     * Отримання історії задачі.
     */
    @GET("api/v1/tasks/history/")
    suspend fun getTaskHistory(
        @Query("task") taskId: Int,
        @Query("cursor") cursor: String? = null
    ): PaginatedResponse<HistoryEventDto>

    /**
     * Додавання пункту чекліста.
     */
    @POST("api/v1/tasks/checklists/")
    suspend fun addChecklistItem(
        @Body request: CreateChecklistItemRequest
    ): ChecklistDto

    /**
     * Оновлення стану пункту чекліста.
     */
    @PATCH("api/v1/tasks/checklists/{checklist_id}/")
    suspend fun updateChecklistItemStatus(
        @Path("checklist_id") checklistId: Int,
        @Body request: UpdateChecklistItemRequest
    ): ChecklistDto
}
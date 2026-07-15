package com.example.coreops.data.repository

import com.example.coreops.data.remote.api.TasksApi
import com.example.coreops.data.remote.models.CommentDto
import com.example.coreops.data.remote.models.CommentRequest
import com.example.coreops.data.remote.models.PaginatedResponse
import com.example.coreops.data.remote.models.SprintDto
import com.example.coreops.data.remote.models.TaskDto
import com.example.coreops.data.remote.models.TaskStatusUpdateRequest
import com.example.coreops.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Фізична реалізація репозиторію, яка ходить у мережу за задачами.
 */
class TaskRepositoryImpl @Inject constructor(
    private val api: TasksApi
) : TaskRepository {

    override suspend fun getTasks(
        projectId: Int,
        priority: String?,
        taskType: String?,
        reporter: Int?,
        assignee: Int?,
        dueDateAfter: String?,
        dueDateBefore: String?,
        dueDate: String?,
        cursor: String?
    ): Result<PaginatedResponse<TaskDto>> {
        return try {
            val response = api.getTasks(
                projectId = projectId,
                priority = priority,
                taskType = taskType,
                reporter = reporter,
                assignee = assignee,
                dueDateAfter = dueDateAfter,
                dueDateBefore = dueDateBefore,
                dueDate = dueDate,
                cursor = cursor
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTaskById(taskId: Int): Result<TaskDto> {
        return try {
            val response = api.getTaskById(taskId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTaskStatus(taskId: Int, newStatus: String): Result<TaskDto> {
        return try {
            // Формує об'єкт запиту з новим статусом
            val request = TaskStatusUpdateRequest(status = newStatus)
            // Відправляє PATCH-запит
            val response = api.updateTaskStatus(taskId, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTaskComments(
        taskId: Int,
        cursor: String?
    ): Result<PaginatedResponse<CommentDto>> {
        return try {
            val response = api.getTaskComments(taskId, cursor)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addTaskComment(taskId: Int, content: String): Result<CommentDto> {
        return try {
            val request = CommentRequest(
                task = taskId,
                content = content
            )
            // Викликає API тільки з тілом запиту
            val response = api.addTaskComment(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createTask(request: com.example.coreops.data.remote.models.CreateTaskRequest): Result<com.example.coreops.data.remote.models.TaskDto> {
        return try {
            val response = api.createTask(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllMyTasks(
        project: Int?, priority: String?, taskType: String?, reporter: Int?,
        assignee: Int?, status: String?, ordering: String?, search: String?,
        dueDateAfter: String?, dueDateBefore: String?, dueDate: String?, cursor: String?
    ): Result<PaginatedResponse<TaskDto>> {
        return try {
            val response = api.getAllMyTasks(
                project = project, priority = priority, taskType = taskType,
                reporter = reporter, assignee = assignee, status = status,
                ordering = ordering, search = search, dueDateAfter = dueDateAfter,
                dueDateBefore = dueDateBefore, dueDate = dueDate, cursor = cursor
            )
            Result.success(response)
        } catch (e: retrofit2.HttpException) {
            Result.failure(Exception("Помилка сервера: ${e.code()}"))
        } catch (e: java.io.IOException) {
            Result.failure(Exception("Помилка підключення до інтернету"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getActiveSprint(projectId: Int): Result<List<SprintDto>> {
        return try {
            // Запит з фільтром на активні спринти
            val response = api.getActiveSprints(projectId = projectId, status = "active")

            Result.success(response.results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
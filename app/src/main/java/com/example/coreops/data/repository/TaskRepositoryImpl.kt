package com.example.coreops.data.repository

import com.example.coreops.data.remote.api.TasksApi
import com.example.coreops.data.remote.models.CommentDto
import com.example.coreops.data.remote.models.CommentRequest
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

    override suspend fun getTasks(projectId: Int): Result<List<TaskDto>> {
        return try {
            val response = api.getTasks(projectId)
            Result.success(response.results) // Витягує сам список із пагінації
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

    override suspend fun getTaskComments(taskId: Int): Result<List<CommentDto>> {
        return try {
            val response = api.getTaskComments(taskId)
            Result.success(response.results)
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
            // Викликаємо API тільки з тілом запиту
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
}
package com.example.coreops.data.repository

import com.example.coreops.data.remote.api.TasksApi
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

            val request = TaskStatusUpdateRequest(status = newStatus)
            val response = api.updateTaskStatus(taskId, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
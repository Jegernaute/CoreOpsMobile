package com.example.coreops.domain.repository

import com.example.coreops.data.remote.models.CommentDto
import com.example.coreops.data.remote.models.TaskDto

/**
 * Контракт для роботи з даними задач.
 */
interface TaskRepository {
    suspend fun getTasks(projectId: Int): Result<List<TaskDto>>
    suspend fun getTaskById(taskId: Int): Result<TaskDto>
    suspend fun updateTaskStatus(taskId: Int, newStatus: String): Result<TaskDto>
    suspend fun getTaskComments(taskId: Int): Result<List<CommentDto>>
    suspend fun addTaskComment(taskId: Int, content: String): Result<CommentDto>
    suspend fun createTask(request: com.example.coreops.data.remote.models.CreateTaskRequest): Result<TaskDto>
    suspend fun getAllMyTasks(): Result<List<TaskDto>>
}
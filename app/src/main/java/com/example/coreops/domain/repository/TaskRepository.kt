package com.example.coreops.domain.repository

import com.example.coreops.data.remote.models.ChecklistDto
import com.example.coreops.data.remote.models.CommentDto
import com.example.coreops.data.remote.models.HistoryEventDto
import com.example.coreops.data.remote.models.PaginatedResponse
import com.example.coreops.data.remote.models.TaskDto
import com.example.coreops.data.remote.models.SprintDto

/**
 * Контракт для роботи з даними задач.
 */
interface TaskRepository {
    suspend fun getTasks(
        projectId: Int,
        priority: String? = null,
        taskType: String? = null,
        reporter: Int? = null,
        assignee: Int? = null,
        dueDateAfter: String? = null,
        dueDateBefore: String? = null,
        dueDate: String? = null,
        cursor: String? = null
    ): Result<PaginatedResponse<TaskDto>>

    suspend fun getTaskById(taskId: Int): Result<TaskDto>
    suspend fun updateTaskStatus(taskId: Int, newStatus: String): Result<TaskDto>
    suspend fun getTaskComments(taskId: Int, cursor: String? = null): Result<PaginatedResponse<CommentDto>>

    suspend fun addTaskComment(taskId: Int, content: String): Result<CommentDto>
    suspend fun createTask(request: com.example.coreops.data.remote.models.CreateTaskRequest): Result<TaskDto>
    suspend fun getAllMyTasks(
        project: Int? = null,
        priority: String? = null,
        taskType: String? = null,
        reporter: Int? = null,
        assignee: Int? = null,
        status: String? = null,
        ordering: String? = null,
        search: String? = null,
        dueDateAfter: String? = null,
        dueDateBefore: String? = null,
        dueDate: String? = null,
        cursor: String? = null
    ): Result<PaginatedResponse<TaskDto>>

    suspend fun getActiveSprint(projectId: Int): Result<List<SprintDto>>

    suspend fun getTaskHistory(taskId: Int, cursor: String? = null): Result<PaginatedResponse<HistoryEventDto>>
    suspend fun addChecklistItem(taskId: Int, content: String): Result<ChecklistDto>
    suspend fun updateChecklistItemStatus(checklistId: Int, isCompleted: Boolean): Result<ChecklistDto>
}
package com.example.coreops.ui.tasks

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coreops.data.remote.models.TaskDto
import com.example.coreops.domain.TaskSyncManager
import com.example.coreops.domain.repository.ProjectRepository
import com.example.coreops.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.coreops.data.remote.models.ProjectMemberDto

data class ProjectTaskFilters(
    val priority: String? = null,
    val taskType: String? = null,
    val assignee: Int? = null,
    val reporter: Int? = null,
    val deadlineFilter: String? = null,
    val dueDate: String? = null,
    val dueDateAfter: String? = null,
    val dueDateBefore: String? = null
)

sealed class ProjectTasksState {
    object Loading : ProjectTasksState()

    data class Success(
        val tasks: List<TaskDto>,
        val projectName: String = "",
        val activeSprintName: String? = null,
        val hasMore: Boolean = false
    ) : ProjectTasksState()

    data class Error(val message: String) : ProjectTasksState()
}

@HiltViewModel
class ProjectTasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository,
    private val savedStateHandle: SavedStateHandle,
    private val syncManager: TaskSyncManager
) : ViewModel() {

    private val _state = MutableStateFlow<ProjectTasksState>(ProjectTasksState.Loading)
    val state: StateFlow<ProjectTasksState> = _state.asStateFlow()

    private val _filters = MutableStateFlow(ProjectTaskFilters())
    val filters: StateFlow<ProjectTaskFilters> = _filters.asStateFlow()

    private val _projectMembers = MutableStateFlow<List<ProjectMemberDto>>(emptyList())
    val projectMembers: StateFlow<List<ProjectMemberDto>> = _projectMembers.asStateFlow()

    private var nextCursor: String? = null
    private var isLoadingMore = false
    private var currentProjectId: Int = 0

    private var cachedProjectName: String = ""
    private var cachedSprintName: String? = null

    init {
        savedStateHandle.get<Int>("projectId")?.let { id ->
            currentProjectId = id
            loadTasks(id)
            loadProjectMetadata(id)
        }

        viewModelScope.launch {
            syncManager.taskUpdates.collect { (updatedTaskId, newStatus) ->
                val currentState = _state.value
                if (currentState is ProjectTasksState.Success) {
                    val updatedList = currentState.tasks.map { task ->
                        if (task.id == updatedTaskId) task.copy(status = newStatus) else task
                    }
                    _state.value = currentState.copy(tasks = updatedList)
                }
            }
        }

        viewModelScope.launch {
            syncManager.serverFetchRequests.collect {
                if (currentProjectId != 0) {
                    loadTasks(currentProjectId, isSilent = true)
                }
            }
        }
    }

    fun loadTasks(projectId: Int, isSilent: Boolean = false) {
        currentProjectId = projectId
        viewModelScope.launch {
            if (!isSilent) {
                _state.value = ProjectTasksState.Loading
            }
            nextCursor = null

            val currentFilters = _filters.value

            taskRepository.getTasks(
                projectId = projectId,
                priority = currentFilters.priority,
                taskType = currentFilters.taskType,
                reporter = currentFilters.reporter,
                assignee = currentFilters.assignee,
                dueDateAfter = currentFilters.dueDateAfter,
                dueDateBefore = currentFilters.dueDateBefore,
                dueDate = currentFilters.dueDate,
                cursor = null
            ).onSuccess { paginatedResponse ->
                nextCursor = extractCursor(paginatedResponse.next)
                _state.value = ProjectTasksState.Success(
                    tasks = paginatedResponse.results,
                    projectName = cachedProjectName,
                    activeSprintName = cachedSprintName,
                    hasMore = nextCursor != null
                )
            }.onFailure { error ->
                if (!isSilent) {
                    _state.value = ProjectTasksState.Error(error.message ?: "Помилка завантаження")
                }
            }
        }
    }

    fun loadMoreTasks() {
        if (isLoadingMore || nextCursor == null) return

        val currentState = _state.value
        if (currentState !is ProjectTasksState.Success) return

        isLoadingMore = true
        viewModelScope.launch {
            val currentFilters = _filters.value

            taskRepository.getTasks(
                projectId = currentProjectId,
                priority = currentFilters.priority,
                taskType = currentFilters.taskType,
                reporter = currentFilters.reporter,
                assignee = currentFilters.assignee,
                dueDateAfter = currentFilters.dueDateAfter,
                dueDateBefore = currentFilters.dueDateBefore,
                dueDate = currentFilters.dueDate,
                cursor = nextCursor
            ).onSuccess { paginatedResponse ->
                val accumulatedTasks = currentState.tasks + paginatedResponse.results
                nextCursor = extractCursor(paginatedResponse.next)

                _state.value = currentState.copy(
                    tasks = accumulatedTasks,
                    hasMore = nextCursor != null
                )
                isLoadingMore = false
            }.onFailure {
                isLoadingMore = false
            }
        }
    }

    fun updateTaskStatus(taskId: Int, newStatus: String) {
        val currentState = _state.value
        if (currentState is ProjectTasksState.Success) {
            val updatedTasks = currentState.tasks.map { task ->
                if (task.id == taskId) task.copy(status = newStatus) else task
            }
            _state.value = currentState.copy(tasks = updatedTasks)
        }

        viewModelScope.launch {
            syncManager.notifyTaskStatusChanged(taskId, newStatus)
        }

        viewModelScope.launch {
            val result = taskRepository.updateTaskStatus(taskId, newStatus)
            result.onSuccess { updatedTaskFromServer ->
                val stateAfterApi = _state.value
                if (stateAfterApi is ProjectTasksState.Success) {
                    val finalTasks = stateAfterApi.tasks.map {
                        if (it.id == taskId) updatedTaskFromServer else it
                    }
                    _state.value = stateAfterApi.copy(tasks = finalTasks)
                }
                viewModelScope.launch {
                    syncManager.triggerServerFetch()
                }
            }.onFailure { error ->
                println("Помилка оновлення статусу: ${error.message}")
                if (currentProjectId != 0) {
                    loadTasks(currentProjectId, isSilent = true)
                }
            }
        }
    }

    fun loadProjectMetadata(projectId: Int) {
        viewModelScope.launch {
            val projectRes = projectRepository.getProjectById(projectId)
            val sprintRes = taskRepository.getActiveSprint(projectId)

            cachedProjectName = projectRes.getOrNull()?.name ?: "Проєкт"
            cachedSprintName = sprintRes.getOrNull()?.firstOrNull()?.name

            projectRes.getOrNull()?.members?.let { members ->
                _projectMembers.value = members.sortedBy { it.userName }
            }

            val currentState = _state.value
            if (currentState is ProjectTasksState.Success) {
                _state.value = currentState.copy(
                    projectName = cachedProjectName,
                    activeSprintName = cachedSprintName
                )
            }
        }
    }

    fun applyFilters(newFilters: ProjectTaskFilters) {
        val processedFilters = processDateFilters(newFilters)
        _filters.value = processedFilters
        loadTasks(currentProjectId)
    }

    fun clearFilters() {
        _filters.value = ProjectTaskFilters()
        loadTasks(currentProjectId)
    }

    private fun processDateFilters(filters: ProjectTaskFilters): ProjectTaskFilters {
        if (filters.deadlineFilter == null) {
            return filters.copy(dueDate = null, dueDateBefore = null, dueDateAfter = null)
        }

        val today = LocalDate.now()
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        return when (filters.deadlineFilter) {
            "today" -> filters.copy(
                dueDate = today.toString(),
                dueDateBefore = null,
                dueDateAfter = null
            )
            "week" -> {
                val start = today.atStartOfDay().format(dateTimeFormatter)
                val end = today.plusDays(7).atTime(23, 59, 59).format(dateTimeFormatter)
                filters.copy(
                    dueDate = null,
                    dueDateAfter = start,
                    dueDateBefore = end
                )
            }
            "overdue" -> {
                val end = today.minusDays(1).atTime(23, 59, 59).format(dateTimeFormatter)
                filters.copy(
                    dueDate = null,
                    dueDateAfter = null,
                    dueDateBefore = end
                )
            }
            else -> filters
        }
    }

    private fun extractCursor(url: String?): String? {
        if (url == null) return null
        return try {
            Uri.parse(url).getQueryParameter("cursor")
        } catch (e: Exception) {
            null
        }
    }
}
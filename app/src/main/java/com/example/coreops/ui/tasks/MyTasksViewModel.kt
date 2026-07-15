package com.example.coreops.ui.tasks

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coreops.data.remote.models.ProjectDto
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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.example.coreops.data.remote.models.ProjectMemberDto
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class TaskFilters(
    val project: Int? = null,
    val priority: String? = null,
    val taskType: String? = null,
    val reporter: Int? = null,
    val assignee: Int? = null,
    val status: String? = null,
    val ordering: String? = null,
    val search: String? = null,
    val dueDateAfter: String? = null,
    val dueDateBefore: String? = null,
    val dueDate: String? = null,
    val deadlineFilter: String? = null
)

sealed class MyTasksState {
    object Loading : MyTasksState()

    data class Success(val tasks: List<TaskDto>, val hasMore: Boolean = false) : MyTasksState()
    data class Error(val message: String) : MyTasksState()
}

@HiltViewModel
class MyTasksViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val projectRepository: ProjectRepository,
    private val syncManager: TaskSyncManager
) : ViewModel() {

    private val _state = MutableStateFlow<MyTasksState>(MyTasksState.Loading)
    val state: StateFlow<MyTasksState> = _state.asStateFlow()

    // Стан для зберігання активних фільтрів
    private val _filters = MutableStateFlow(TaskFilters())
    val filters: StateFlow<TaskFilters> = _filters.asStateFlow()

    // Стан для списку проєктів
    private val _projects = MutableStateFlow<List<ProjectDto>>(emptyList())
    val projects: StateFlow<List<ProjectDto>> = _projects.asStateFlow()

    // Унікальний список користувачів з усіх завантажених проєктів
    val availableMembers: StateFlow<List<ProjectMemberDto>> = _projects.map { projectsList ->
        projectsList.flatMap { it.members }
            .distinctBy { it.userId }
            .sortedBy { it.userName }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var nextCursor: String? = null
    private var isLoadingMore = false

    init {
        fetchMyTasks()
        fetchProjects()

        viewModelScope.launch {
            syncManager.taskUpdates.collect { (updatedTaskId, newStatus) ->
                val currentState = _state.value
                if (currentState is MyTasksState.Success) {
                    val updatedList = currentState.tasks.map { task ->
                        if (task.id == updatedTaskId) task.copy(status = newStatus) else task
                    }
                    _state.value = currentState.copy(tasks = updatedList)
                }
            }
        }

        viewModelScope.launch {
            syncManager.serverFetchRequests.collect {
                fetchMyTasks()
            }
        }
    }

    // Метод завантаження проєктів
    private fun fetchProjects() {
        viewModelScope.launch {
            // Викликає метод репозиторію
            projectRepository.getProjects(cursor = null).onSuccess { paginatedResponse ->
                // Дістає масив проєктів з поля results
                _projects.value = paginatedResponse.results
            }
        }
    }

    // Застосування нових фільтрів і оновлення списку
    fun applyFilters(newFilters: TaskFilters) {
        val processedFilters = processDateFilters(newFilters)
        _filters.value = processedFilters
        fetchMyTasks()
    }

    // Конвертує текстовий вибір UI у реальні дати для бекенду
    private fun processDateFilters(filters: TaskFilters): TaskFilters {
        if (filters.deadlineFilter == null) {
            return filters.copy(dueDate = null, dueDateBefore = null, dueDateAfter = null)
        }

        val today = LocalDate.now()
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        return when (filters.deadlineFilter) {
            "today" -> {
                // Тільки сьогоднішні задачі
                filters.copy(
                    dueDate = today.toString(),
                    dueDateBefore = null,
                    dueDateAfter = null
                )
            }
            "week" -> {
                // Від сьогодні (початок дня) до кінця тижня (+7 днів)
                val start = today.atStartOfDay().format(dateTimeFormatter)
                val end = today.plusDays(7).atTime(23, 59, 59).format(dateTimeFormatter)
                filters.copy(
                    dueDate = null,
                    dueDateAfter = start,
                    dueDateBefore = end
                )
            }
            "overdue" -> {
                // Все де дедлайн був до вчорашнього вечора 23:59:59
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

    // Скидання всіх фільтрів
    fun clearFilters() {
        _filters.value = TaskFilters()
        fetchMyTasks()
    }

    fun fetchMyTasks(isSilent: Boolean = false) {
        viewModelScope.launch {
            if (!isSilent) {
                _state.value = MyTasksState.Loading
            }
            nextCursor = null

            val currentFilters = _filters.value // Бере поточні фільтри

            repository.getAllMyTasks(
                project = currentFilters.project,
                priority = currentFilters.priority,
                taskType = currentFilters.taskType,
                reporter = currentFilters.reporter,
                assignee = currentFilters.assignee,
                status = currentFilters.status,
                ordering = currentFilters.ordering,
                search = currentFilters.search,
                dueDateAfter = currentFilters.dueDateAfter,
                dueDateBefore = currentFilters.dueDateBefore,
                dueDate = currentFilters.dueDate,
                cursor = null
            ).collectResult()
        }
    }

    fun loadMoreTasks() {
        if (isLoadingMore || nextCursor == null) return

        val currentState = _state.value
        if (currentState !is MyTasksState.Success) return

        isLoadingMore = true
        viewModelScope.launch {
            val currentFilters = _filters.value // Бере поточні фільтри для підвантаження наступної сторінки

            repository.getAllMyTasks(
                project = currentFilters.project,
                priority = currentFilters.priority,
                taskType = currentFilters.taskType,
                reporter = currentFilters.reporter,
                assignee = currentFilters.assignee,
                status = currentFilters.status,
                ordering = currentFilters.ordering,
                search = currentFilters.search,
                dueDateAfter = currentFilters.dueDateAfter,
                dueDateBefore = currentFilters.dueDateBefore,
                dueDate = currentFilters.dueDate,
                cursor = nextCursor
            ).onSuccess { paginatedResponse ->
                val accumulatedTasks = currentState.tasks + paginatedResponse.results
                nextCursor = extractCursor(paginatedResponse.next)

                _state.value = MyTasksState.Success(
                    tasks = accumulatedTasks,
                    hasMore = nextCursor != null
                )
                isLoadingMore = false
            }.onFailure {
                isLoadingMore = false
            }
        }
    }

    // помічник з урахуванням PaginatedResponse
    private fun Result<com.example.coreops.data.remote.models.PaginatedResponse<TaskDto>>.collectResult() {
        this.onSuccess { paginatedResponse ->
            nextCursor = extractCursor(paginatedResponse.next)
            _state.value = MyTasksState.Success(
                tasks = paginatedResponse.results,
                hasMore = nextCursor != null
            )
        }.onFailure { error ->
            _state.value = MyTasksState.Error(error.message ?: "Невідома помилка при завантаженні задач")
        }
    }

    // Функція яка дістає чистий токен курсора з повного URL-рядка next
    private fun extractCursor(url: String?): String? {
        if (url == null) return null
        return try {
            Uri.parse(url).getQueryParameter("cursor")
        } catch (e: Exception) {
            null
        }
    }

    fun updateTaskStatus(taskId: Int, newStatus: String) {
        val currentState = _state.value
        if (currentState is MyTasksState.Success) {
            val updatedTasks = currentState.tasks.map { task ->
                if (task.id == taskId) task.copy(status = newStatus) else task
            }
            _state.value = currentState.copy(tasks = updatedTasks)
        }

        viewModelScope.launch {
            syncManager.notifyTaskStatusChanged(taskId, newStatus)
        }

        viewModelScope.launch {
            val result = repository.updateTaskStatus(taskId, newStatus)
            result.onSuccess { updatedTaskFromServer ->
                val stateAfterApi = _state.value
                if (stateAfterApi is MyTasksState.Success) {
                    val finalTasks = stateAfterApi.tasks.map {
                        if (it.id == taskId) updatedTaskFromServer else it
                    }
                    _state.value = stateAfterApi.copy(tasks = finalTasks)
                }
                viewModelScope.launch {
                    syncManager.triggerServerFetch()
                }
            }.onFailure { error ->
                fetchMyTasks()
            }
        }
    }
}
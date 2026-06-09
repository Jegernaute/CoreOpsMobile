package com.example.coreops.ui.tasks

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coreops.data.remote.models.TaskDto
import com.example.coreops.domain.TaskSyncManager
import com.example.coreops.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProjectTasksState {
    object Loading : ProjectTasksState()
    data class Success(val tasks: List<TaskDto>, val hasMore: Boolean = false) : ProjectTasksState()
    data class Error(val message: String) : ProjectTasksState()
}

@HiltViewModel
class ProjectTasksViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val savedStateHandle: SavedStateHandle,
    private val syncManager: TaskSyncManager
) : ViewModel() {

    private val _state = MutableStateFlow<ProjectTasksState>(ProjectTasksState.Loading)
    val state: StateFlow<ProjectTasksState> = _state.asStateFlow()

    // Змінні для керування станом пагінації
    private var nextCursor: String? = null
    private var isLoadingMore = false
    private var currentProjectId: Int = 0

    init {
        val projectId = savedStateHandle.get<Int>("projectId")
            ?: savedStateHandle.get<String>("projectId")?.toIntOrNull()

        if (projectId != null && projectId != 0) {
            currentProjectId = projectId
            loadTasks(projectId)
        } else {
            _state.value = ProjectTasksState.Error("Невірний ID проєкту")
        }

        viewModelScope.launch {
            syncManager.serverFetchRequests.collect {
                if (currentProjectId != 0) loadTasks(currentProjectId)
            }
        }
    }

    // Первинне завантаження або оновлення списку
    fun loadTasks(projectId: Int) {
        currentProjectId = projectId
        viewModelScope.launch {
            _state.value = ProjectTasksState.Loading
            nextCursor = null

            repository.getTasks(projectId, cursor = null).onSuccess { paginatedResponse ->
                nextCursor = extractCursor(paginatedResponse.next)
                _state.value = ProjectTasksState.Success(
                    tasks = paginatedResponse.results,
                    hasMore = nextCursor != null
                )
            }.onFailure { error ->
                _state.value = ProjectTasksState.Error(error.message ?: "Помилка завантаження")
            }
        }
    }

    // Підвантаження наступної сторінки при скролі
    fun loadMoreTasks() {
        if (isLoadingMore || nextCursor == null || currentProjectId == 0) return

        val currentState = _state.value
        if (currentState !is ProjectTasksState.Success) return

        isLoadingMore = true
        viewModelScope.launch {
            repository.getTasks(currentProjectId, cursor = nextCursor).onSuccess { paginatedResponse ->
                val accumulatedTasks = currentState.tasks + paginatedResponse.results
                nextCursor = extractCursor(paginatedResponse.next)

                _state.value = ProjectTasksState.Success(
                    tasks = accumulatedTasks,
                    hasMore = nextCursor != null
                )
                isLoadingMore = false
            }.onFailure {
                isLoadingMore = false
            }
        }
    }

    // Витягує токен курсора з URL
    private fun extractCursor(url: String?): String? {
        if (url == null) return null
        return try {
            Uri.parse(url).getQueryParameter("cursor")
        } catch (e: Exception) {
            null
        }
    }

    // Оновлення статусу задачі
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
            val result = repository.updateTaskStatus(taskId, newStatus)
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
                    loadTasks(currentProjectId)
                }
            }
        }
    }
}
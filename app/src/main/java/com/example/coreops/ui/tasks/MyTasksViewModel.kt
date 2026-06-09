package com.example.coreops.ui.tasks

import android.net.Uri
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

sealed class MyTasksState {
    object Loading : MyTasksState()

    data class Success(val tasks: List<TaskDto>, val hasMore: Boolean = false) : MyTasksState()
    data class Error(val message: String) : MyTasksState()
}

@HiltViewModel
class MyTasksViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val syncManager: TaskSyncManager
) : ViewModel() {

    private val _state = MutableStateFlow<MyTasksState>(MyTasksState.Loading)
    val state: StateFlow<MyTasksState> = _state.asStateFlow()

    private var nextCursor: String? = null
    private var isLoadingMore = false

    init {
        fetchMyTasks()

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

    // Первинне завантаження або оновлення списку (Pull-to-Refresh)
    fun fetchMyTasks() {
        viewModelScope.launch {
            _state.value = MyTasksState.Loading
            nextCursor = null // Скидає курсор при чистому завантаженні

            repository.getAllMyTasks(cursor = null).collectResult()
        }
    }

    // Функція підвантаження наступної сторінки при скролі
    fun loadMoreTasks() {
        if (isLoadingMore || nextCursor == null) return

        val currentState = _state.value
        if (currentState !is MyTasksState.Success) return

        isLoadingMore = true
        viewModelScope.launch {
            repository.getAllMyTasks(cursor = nextCursor).onSuccess { paginatedResponse ->
                // Зливає старі задачі з новими результатами
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
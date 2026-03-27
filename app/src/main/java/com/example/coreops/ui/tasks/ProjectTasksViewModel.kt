package com.example.coreops.ui.tasks

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

// Стан екрану (Loading, Success, Error)
sealed class ProjectTasksState {
    object Loading : ProjectTasksState()
    data class Success(val tasks: List<TaskDto>) : ProjectTasksState()
    data class Error(val message: String) : ProjectTasksState()
}

@HiltViewModel
class ProjectTasksViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val savedStateHandle: SavedStateHandle, // Отримання аргументів з навігації
    private val syncManager: TaskSyncManager
) : ViewModel() {

    // Внутрішній (змінний) та зовнішній (тільки для читання) стан
    private val _state = MutableStateFlow<ProjectTasksState>(ProjectTasksState.Loading)
    val state: StateFlow<ProjectTasksState> = _state.asStateFlow()

    init {
        // Витягує projectId з ключа, який вказувався у MainScreen.kt ("projectId")
        val projectId = savedStateHandle.get<Int>("projectId")

        if (projectId != null && projectId != 0) {
            loadTasks(projectId)
        } else {
            _state.value = ProjectTasksState.Error("Помилка: Не вдалося отримати ID проєкту")
        }

        viewModelScope.launch {
            syncManager.taskUpdates.collect { (updatedTaskId, newStatus) ->
                val currentState = _state.value
                if (currentState is ProjectTasksState.Success) {
                    val updatedList = currentState.tasks.map { task ->
                        if (task.id == updatedTaskId) task.copy(status = newStatus) else task
                    }
                    _state.value = ProjectTasksState.Success(updatedList)
                }
            }
        }
    }

    private fun loadTasks(projectId: Int) {
        viewModelScope.launch {
            _state.value = ProjectTasksState.Loading

            // Викликає метод репозиторію
            val result = repository.getTasks(projectId)

            // Обробляє результат
            result.onSuccess { tasks ->
                _state.value = ProjectTasksState.Success(tasks)
            }
            result.onFailure { error ->
                _state.value = ProjectTasksState.Error(error.message ?: "Сталася невідома помилка")
            }
        }
    }

    fun updateTaskStatus(taskId: Int, newStatus: String) {
        // 1. МИТТЄВЕ ОНОВЛЕННЯ UI
        val currentState = _state.value
        if (currentState is ProjectTasksState.Success) {
            val updatedTasks = currentState.tasks.map { task ->
                if (task.id == taskId) {
                    task.copy(status = newStatus)
                } else {
                    task
                }
            }
            _state.value = ProjectTasksState.Success(updatedTasks)
        }

        viewModelScope.launch {
            syncManager.notifyTaskStatusChanged(taskId, newStatus)
        }

        // 2. ФОНОВИЙ ЗАПИТ НА СЕРВЕР
        viewModelScope.launch {
            val result = repository.updateTaskStatus(taskId, newStatus)

            result.onSuccess { updatedTaskFromServer ->
                val stateAfterApi = _state.value
                if (stateAfterApi is ProjectTasksState.Success) {
                    val finalTasks = stateAfterApi.tasks.map {
                        if (it.id == taskId) updatedTaskFromServer else it
                    }
                    _state.value = ProjectTasksState.Success(finalTasks)
                }
            }
            result.onFailure { error ->
                println("Помилка оновлення статусу: ${error.message}")
                val projectId = savedStateHandle.get<Int>("projectId")
                if (projectId != null && projectId != 0) {
                    loadTasks(projectId)
                }
            }
        }
    }
}
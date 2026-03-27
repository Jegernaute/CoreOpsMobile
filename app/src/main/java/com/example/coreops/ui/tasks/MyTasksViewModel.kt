package com.example.coreops.ui.tasks

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
    data class Success(val tasks: List<TaskDto>) : MyTasksState()
    data class Error(val message: String) : MyTasksState()
}

@HiltViewModel
class MyTasksViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val syncManager: TaskSyncManager
) : ViewModel() {

    private val _state = MutableStateFlow<MyTasksState>(MyTasksState.Loading)
    val state: StateFlow<MyTasksState> = _state.asStateFlow()

    init {
        fetchMyTasks()

        viewModelScope.launch {
            syncManager.taskUpdates.collect { (updatedTaskId, newStatus) ->
                val currentState = _state.value
                if (currentState is MyTasksState.Success) {
                    val updatedList = currentState.tasks.map { task ->
                        if (task.id == updatedTaskId) task.copy(status = newStatus) else task
                    }
                    _state.value = MyTasksState.Success(updatedList)
                }
            }
        }
    }

    fun fetchMyTasks() {
        viewModelScope.launch {
            _state.value = MyTasksState.Loading

            val result = repository.getAllMyTasks()

            result.fold(
                onSuccess = { tasks ->
                    _state.value = MyTasksState.Success(tasks)
                },
                onFailure = { error ->
                    _state.value = MyTasksState.Error(error.message ?: "Невідома помилка при завантаженні задач")
                }
            )
        }
    }

    // Функція для зміни статусу задачі
    fun updateTaskStatus(taskId: Int, newStatus: String) {
        val currentState = _state.value
        if (currentState is MyTasksState.Success) {
            val updatedTasks = currentState.tasks.map { task ->
                if (task.id == taskId) task.copy(status = newStatus) else task
            }
            _state.value = MyTasksState.Success(updatedTasks)
        }

        // --- 3. ПОВІДОМЛЯЄ ІНШІ ЕКРАНИ ПРО ЗМІНУ ---
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
                    _state.value = MyTasksState.Success(finalTasks)
                }
            }
            result.onFailure { fetchMyTasks() }
        }
    }
}
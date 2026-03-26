package com.example.coreops.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coreops.data.remote.models.TaskDto
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
    private val repository: TaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow<MyTasksState>(MyTasksState.Loading)
    val state: StateFlow<MyTasksState> = _state.asStateFlow()

    init {
        fetchMyTasks()
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
        viewModelScope.launch {
            val result = repository.updateTaskStatus(taskId, newStatus)

            result.onSuccess {
                fetchMyTasks()
            }
            result.onFailure { error ->
                println("Помилка оновлення статусу: ${error.message}")
            }
        }
    }
}
package com.example.coreops.ui.tasks

import androidx.lifecycle.SavedStateHandle
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

// Стан екрану деталей задачі
sealed class TaskDetailState {
    object Loading : TaskDetailState()
    data class Success(val task: TaskDto) : TaskDetailState()
    data class Error(val message: String) : TaskDetailState()
}

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val repository: TaskRepository,
    savedStateHandle: SavedStateHandle // Для отримання taskId з навігації
) : ViewModel() {

    private val _state = MutableStateFlow<TaskDetailState>(TaskDetailState.Loading)
    val state: StateFlow<TaskDetailState> = _state.asStateFlow()

    // Зберігає ID задачі на рівні класу, щоб використовувати його у функції оновлення
    private val taskId: Int? = savedStateHandle.get<Int>("taskId")

    init {
        // Запит при старті
        if (taskId != null && taskId != 0) {
            loadTask(taskId)
        } else {
            _state.value = TaskDetailState.Error("Помилка: Не вдалося отримати ID задачі з навігації")
        }
    }

    private fun loadTask(id: Int) {
        viewModelScope.launch {
            _state.value = TaskDetailState.Loading

            val result = repository.getTaskById(id)

            result.onSuccess { task ->
                _state.value = TaskDetailState.Success(task)
            }
            result.onFailure { error ->
                _state.value = TaskDetailState.Error(error.message ?: "Сталася невідома помилка")
            }
        }
    }

    /**
     * Функція для оновлення статусу задачі на бекенді.
     * Викликається з UI, коли користувач змінює статус (наприклад, через DropdownMenu).
     */
    fun updateStatus(newStatus: String) {
        if (taskId == null || taskId == 0) return

        viewModelScope.launch {
            val result = repository.updateTaskStatus(taskId, newStatus)

            result.onSuccess { updatedTask ->
                _state.value = TaskDetailState.Success(updatedTask)
            }
            result.onFailure { error ->
                println("Помилка оновлення статусу: ${error.message}")
            }
        }
    }
}
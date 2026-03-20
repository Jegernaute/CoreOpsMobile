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

// Стани екранів
sealed class TaskDetailState {
    object Loading : TaskDetailState()
    data class Success(val task: TaskDto) : TaskDetailState()
    data class Error(val message: String) : TaskDetailState()
}

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val repository: TaskRepository,
    savedStateHandle: SavedStateHandle // Витягує параметри з NavHost
) : ViewModel() {

    private val _state = MutableStateFlow<TaskDetailState>(TaskDetailState.Loading)
    val state: StateFlow<TaskDetailState> = _state.asStateFlow()

    init {
        // Авто запуск
        // Витягує ID задачі з маршруту "task_detail/{taskId}"
        val taskId: Int? = savedStateHandle.get<Int>("taskId")

        if (taskId != null) {
            loadTask(taskId)
        } else {
            _state.value = TaskDetailState.Error("Помилка: не вдалося отримати ID задачі з навігації")
        }
    }

    // Завантаження деталей задачі
    fun loadTask(taskId: Int) {
        viewModelScope.launch {
            _state.value = TaskDetailState.Loading


            val result = repository.getTaskById(taskId)

            result.fold(
                onSuccess = { task ->
                    _state.value = TaskDetailState.Success(task)
                },
                onFailure = { exception ->
                    _state.value = TaskDetailState.Error(
                        exception.localizedMessage ?: "Невідома помилка при завантаженні деталей задачі"
                    )
                }
            )
        }
    }
}
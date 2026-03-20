package com.example.coreops.ui.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coreops.data.remote.models.TaskDto
import com.example.coreops.domain.model.TaskStatus
import com.example.coreops.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Стани екрану
sealed class ProjectTasksState {
    object Loading : ProjectTasksState()
    data class Success(val tasks: List<TaskDto>) : ProjectTasksState()
    data class Error(val message: String) : ProjectTasksState()
}

@HiltViewModel
class ProjectTasksViewModel @Inject constructor(
    private val repository: TaskRepository,
    savedStateHandle: SavedStateHandle // Магічний об'єкт, який тримає аргументи навігації
) : ViewModel() {

    private val _state = MutableStateFlow<ProjectTasksState>(ProjectTasksState.Loading)
    val state: StateFlow<ProjectTasksState> = _state.asStateFlow()

    init {
        // Авто запуск
        // Витягує ID проєкту безпосередньо з посилання "project_tasks/{projectId}"
        val projectId: Int? = savedStateHandle.get<Int>("projectId")

        if (projectId != null) {
            loadTasks(projectId)
        } else {
            _state.value = ProjectTasksState.Error("Помилка: не вдалося отримати ID проєкту")
        }
    }

    // Завантаження даних
    fun loadTasks(projectId: Int) {
        viewModelScope.launch {
            _state.value = ProjectTasksState.Loading

            val result = repository.getTasks(projectId)

            result.fold(
                onSuccess = { tasks ->
                    _state.value = ProjectTasksState.Success(tasks)
                },
                onFailure = { exception ->
                    _state.value = ProjectTasksState.Error(
                        exception.localizedMessage ?: "Невідома помилка при завантаженні задач"
                    )
                }
            )
        }
    }

    /**
     * Змінює статус задачі на сервері та локально оновлює UI.
     */
    fun changeTaskStatus(taskId: Int, newStatus: TaskStatus) {
        viewModelScope.launch {
            // 1. Звертається до репозиторію.
            // Передає .apiValue бо сервер очікує рядок (напр. "in_progress")
            val result = repository.updateTaskStatus(taskId, newStatus.apiValue)

            result.fold(
                onSuccess = { updatedTask ->
                    // 2. Якщо сервер відповів успішно (200 OK) бере поточний стан
                    val currentState = _state.value

                    if (currentState is ProjectTasksState.Success) {
                        // 3. Знаходить задачу у списку і замінює її на ту, що повернув сервер
                        val updatedList = currentState.tasks.map { task ->
                            if (task.id == taskId) updatedTask else task
                        }

                        // 4. Емітить новий список у StateFlow.
                        // Compose побачить зміни і миттєво перемалює цю одну картку
                        _state.value = ProjectTasksState.Success(updatedList)
                    }
                },
                onFailure = { exception ->
                    // TODO: Додати відображення повідомлення про помилку (Snackbar/Toast)

                }
            )
        }
    }
}
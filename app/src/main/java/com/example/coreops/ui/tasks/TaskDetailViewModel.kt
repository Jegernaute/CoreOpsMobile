package com.example.coreops.ui.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coreops.data.remote.models.TaskDto
import com.example.coreops.domain.model.TaskStatus
import com.example.coreops.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
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

    //Канал для одноразових подій (наприклад, Toast-повідомлень)
    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent: SharedFlow<String> = _uiEvent.asSharedFlow()

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

    /**
     * Змінює статус задачі на сервері та локально оновлює екран деталей.
     */
    fun updateStatus(newStatus: TaskStatus) {
        val currentState = _state.value
        // Переконується що має завантажену задачу
        if (currentState is TaskDetailState.Success) {
            val taskId = currentState.task.id

            viewModelScope.launch {
                // Викликає  готовий метод репозиторію
                val result = repository.updateTaskStatus(taskId, newStatus.apiValue)

                result.fold(
                    onSuccess = { updatedTask ->
                        // Якщо сервер сказав "ОК", просто підміняє задачу в стейті
                        _state.value = TaskDetailState.Success(updatedTask)
                    },
                    onFailure = { exception ->
                        // Базове повідомлення
                        var errorMessage = "Не вдалося оновити статус"

                        // Перевіряє чи це помилка від сервера (4xx або 5xx)
                        if (exception is HttpException) {
                            val errorBody = exception.response()?.errorBody()?.string()
                            if (!errorBody.isNullOrEmpty()) {
                                try {
                                    // Парсить JSON який повернув Django
                                    val json = JSONObject(errorBody)
                                    // Шукає масив "non_field_errors" (як у лозі)
                                    if (json.has("non_field_errors")) {
                                        errorMessage = json.getJSONArray("non_field_errors").getString(0)
                                    }
                                } catch (e: Exception) {
                                    // Якщо JSON якось відрізняється ігнорує і залишає базове повідомлення
                                }
                            }
                        }

                        // Відправляє повідомлення в UI
                        _uiEvent.emit(errorMessage)
                    }
                )
            }
        }
    }
}
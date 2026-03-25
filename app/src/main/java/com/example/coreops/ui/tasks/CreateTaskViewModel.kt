package com.example.coreops.ui.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coreops.data.remote.models.CreateTaskRequest
import com.example.coreops.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Стани для процесу відправки
sealed class CreateTaskState {
    object Idle : CreateTaskState()
    object Loading : CreateTaskState()
    object Success : CreateTaskState()
    data class Error(val message: String) : CreateTaskState()
}

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 1. Витягує ID проєкту з аргументів навігації (безпечно обробляємо Int та String)
    private val projectId: Int = savedStateHandle.get<Int>("projectId")
        ?: savedStateHandle.get<String>("projectId")?.toIntOrNull()
        ?: throw IllegalArgumentException("Помилка: Не передано ID проєкту")

    // 2. Стани для полів форми
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _taskType = MutableStateFlow("task")
    val taskType: StateFlow<String> = _taskType.asStateFlow()

    private val _priority = MutableStateFlow("medium")
    val priority: StateFlow<String> = _priority.asStateFlow()

    // 3. Стан самого процесу створення (для лоадера та помилок)
    private val _uiState = MutableStateFlow<CreateTaskState>(CreateTaskState.Idle)
    val uiState: StateFlow<CreateTaskState> = _uiState.asStateFlow()


    fun setTitle(newTitle: String) {
        _title.value = newTitle
        clearError()
    }
    fun setDescription(newDesc: String) { _description.value = newDesc }
    fun setTaskType(newType: String) { _taskType.value = newType }
    fun setPriority(newPriority: String) { _priority.value = newPriority }

    // Відправка даних
    fun submitTask() {
        val currentTitle = _title.value.trim()

        // Базова валідація
        if (currentTitle.isBlank()) {
            _uiState.value = CreateTaskState.Error("Назва задачі є обов'язковою")
            return
        }

        viewModelScope.launch {
            _uiState.value = CreateTaskState.Loading

            val request = CreateTaskRequest(
                title = currentTitle,
                description = _description.value.trim().takeIf { it.isNotEmpty() },
                project = projectId,
                taskType = _taskType.value,
                priority = _priority.value,
                status = "to_do" // Завжди створюємо з цим статусом
            )

            val result = repository.createTask(request)

            result.onSuccess {
                _uiState.value = CreateTaskState.Success
            }.onFailure { error ->
                _uiState.value = CreateTaskState.Error(
                    error.message ?: "Сталася помилка при створенні задачі"
                )
            }
        }
    }

    // Скидання помилки
    private fun clearError() {
        if (_uiState.value is CreateTaskState.Error) {
            _uiState.value = CreateTaskState.Idle
        }
    }
}

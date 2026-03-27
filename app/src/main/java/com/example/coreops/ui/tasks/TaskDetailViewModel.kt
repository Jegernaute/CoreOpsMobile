package com.example.coreops.ui.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coreops.data.remote.models.CommentDto
import com.example.coreops.data.remote.models.TaskDto
import com.example.coreops.domain.TaskSyncManager
import com.example.coreops.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TaskDetailState {
    object Loading : TaskDetailState()
    data class Success(val task: TaskDto, val comments: List<CommentDto>) : TaskDetailState()
    data class Error(val message: String) : TaskDetailState()
}

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val repository: TaskRepository,
    savedStateHandle: SavedStateHandle,
    private val syncManager: TaskSyncManager
) : ViewModel() {

    private val _state = MutableStateFlow<TaskDetailState>(TaskDetailState.Loading)
    val state: StateFlow<TaskDetailState> = _state.asStateFlow()

    private val _isSendingComment = MutableStateFlow(false)
    val isSendingComment: StateFlow<Boolean> = _isSendingComment.asStateFlow()

    private val taskId: Int? = savedStateHandle.get<Int>("taskId")

    init {
        if (taskId != null && taskId != 0) {
            loadTaskAndComments(taskId)
        } else {
            _state.value = TaskDetailState.Error("Помилка: Не вдалося отримати ID задачі з навігації")
        }
    }

    private fun loadTaskAndComments(id: Int) {
        viewModelScope.launch {
            _state.value = TaskDetailState.Loading

            val taskResult = repository.getTaskById(id)
            val commentsResult = repository.getTaskComments(id)

            if (taskResult.isSuccess && commentsResult.isSuccess) {
                _state.value = TaskDetailState.Success(
                    task = taskResult.getOrNull()!!,
                    comments = commentsResult.getOrNull()!!
                )
            } else {
                // Якщо хоча б один впав - показуємо помилку
                val errorMsg = taskResult.exceptionOrNull()?.message
                    ?: commentsResult.exceptionOrNull()?.message
                    ?: "Сталася невідома помилка при завантаженні"
                _state.value = TaskDetailState.Error(errorMsg)
            }
        }
    }

    fun updateStatus(newStatus: String) {
        if (taskId == null || taskId == 0) return

        // 1. МИТТЄВЕ ОНОВЛЕННЯ UI (Оптимістичний підхід)
        val currentState = _state.value
        if (currentState is TaskDetailState.Success) {
            // Створює копію поточної задачі з новим статусом
            val optimisticTask = currentState.task.copy(status = newStatus)
            // Одразу перезаписує StateFlow, щоб Compose миттєво перемалював екран
            _state.value = currentState.copy(task = optimisticTask)
        }

        viewModelScope.launch {
            syncManager.notifyTaskStatusChanged(taskId, newStatus)
        }

        // 2. ФОНОВИЙ ЗАПИТ НА СЕРВЕР
        viewModelScope.launch {
            val result = repository.updateTaskStatus(taskId, newStatus)

            result.onSuccess { updatedTaskFromServer ->
                // Коли сервер відповідає успішно (200 OK) може тихо оновити задачу
                // фінальними даними з бази (наприклад якщо змінився час updated_at)
                val stateAfterApi = _state.value
                if (stateAfterApi is TaskDetailState.Success) {
                    _state.value = stateAfterApi.copy(task = updatedTaskFromServer)
                }
                viewModelScope.launch {
                    syncManager.triggerServerFetch()
                }
            }
            result.onFailure { error ->
                println("Помилка оновлення статусу: ${error.message}")
                // 3. ВІДКАТ (Rollback): Якщо сталася помилка (наприклад, зник інтернет),
                // завантажує реальні дані знову щоб повернути правильний статус на екран
                loadTaskAndComments(taskId)
            }
        }
    }

    /**
     * Відправка нового коментаря
     */
    fun sendComment(content: String) {
        if (taskId == null || taskId == 0 || content.isBlank()) return

        viewModelScope.launch {
            _isSendingComment.value = true

            val result = repository.addTaskComment(taskId, content)

            result.onSuccess { newComment ->
                val currentState = _state.value
                if (currentState is TaskDetailState.Success) {
                    val updatedComments = currentState.comments + newComment
                    _state.value = currentState.copy(comments = updatedComments)
                }
            }
            result.onFailure { error ->
                println("Помилка відправки коментаря: ${error.message}")
            }

            _isSendingComment.value = false
        }
    }
}
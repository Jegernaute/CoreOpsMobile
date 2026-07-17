package com.example.coreops.ui.tasks

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coreops.data.remote.models.CommentDto
import com.example.coreops.data.remote.models.HistoryEventDto
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
    data class Success(val task: TaskDto,
                       val comments: List<CommentDto>,
                       val history: List<HistoryEventDto>
    ) : TaskDetailState()
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
        taskId?.let { loadFullTaskData(it) }
    }

    fun loadFullTaskData(id: Int) {
        viewModelScope.launch {
            _state.value = TaskDetailState.Loading

            // Завантажує все паралельно
            val taskResult = repository.getTaskById(id)
            val commentsResult = repository.getTaskComments(id)
            val historyResult = repository.getTaskHistory(id)

            if (taskResult.isSuccess) {
                val task = taskResult.getOrThrow()

                _state.value = TaskDetailState.Success(
                    task = task,
                    comments = commentsResult.getOrNull()?.results ?: emptyList(),
                    history = historyResult.getOrNull()?.results ?: emptyList()
                )
            } else {
                _state.value = TaskDetailState.Error(taskResult.exceptionOrNull()?.message ?: "Помилка")
            }
        }
    }

    fun toggleChecklistItem(checklistId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateChecklistItemStatus(checklistId, !isCompleted)
                .onSuccess { if (taskId != null) loadFullTaskData(taskId) }
        }
    }

    fun addChecklistItem(content: String) {
        if (taskId == null) return
        viewModelScope.launch {
            repository.addChecklistItem(taskId, content)
                .onSuccess { loadFullTaskData(taskId) }
        }
    }

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
            _isSendingComment.value = false
        }
    }

    fun updateTaskStatus(taskId: Int, newStatus: String) {
        val currentState = _state.value
        if (currentState is TaskDetailState.Success && currentState.task.id == taskId) {
            _state.value = currentState.copy(task = currentState.task.copy(status = newStatus))
        }

        viewModelScope.launch {
            syncManager.notifyTaskStatusChanged(taskId, newStatus)
        }

        viewModelScope.launch {
            val result = repository.updateTaskStatus(taskId, newStatus)

            result.onSuccess { updatedTaskFromServer ->
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
                loadFullTaskData(taskId)
            }
        }
    }
}
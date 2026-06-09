package com.example.coreops.ui.tasks

import android.net.Uri
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
        taskId?.let { loadTaskAndComments(it) }
    }

    fun loadTaskAndComments(taskId: Int) {
        viewModelScope.launch {
            _state.value = TaskDetailState.Loading

            val taskResult = repository.getTaskById(taskId)
            val commentsResult = repository.getTaskComments(taskId, cursor = null)

            if (taskResult.isSuccess && commentsResult.isSuccess) {
                val task = taskResult.getOrThrow()
                val commentsResponse = commentsResult.getOrThrow()

                _state.value = TaskDetailState.Success(
                    task = task,
                    comments = commentsResponse.results
                )
            } else {
                val errorMsg = taskResult.exceptionOrNull()?.message
                    ?: commentsResult.exceptionOrNull()?.message
                    ?: "Помилка завантаження даних"
                _state.value = TaskDetailState.Error(errorMsg)
            }
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
                loadTaskAndComments(taskId)
            }
        }
    }
}
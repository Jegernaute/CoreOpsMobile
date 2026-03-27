package com.example.coreops.ui.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coreops.data.remote.models.CreateTaskRequest
import com.example.coreops.domain.repository.TaskRepository
import com.example.coreops.domain.repository.ProjectRepository
import com.example.coreops.data.remote.models.ProjectDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CreateTaskState {
    object Idle : CreateTaskState()
    object Loading : CreateTaskState()
    object Success : CreateTaskState()
    data class Error(val message: String) : CreateTaskState()
}

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val projectRepository: ProjectRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val initialProjectId: Int = savedStateHandle.get<Int>("projectId")
        ?: savedStateHandle.get<String>("projectId")?.toIntOrNull()
        ?: 0

    private val _projects = MutableStateFlow<List<ProjectDto>>(emptyList())
    val projects: StateFlow<List<ProjectDto>> = _projects.asStateFlow()

    private val _selectedProjectId = MutableStateFlow<Int?>(if (initialProjectId != 0) initialProjectId else null)
    val selectedProjectId: StateFlow<Int?> = _selectedProjectId.asStateFlow()

    private val _selectedAssigneeId = MutableStateFlow<Int?>(null)
    val selectedAssigneeId: StateFlow<Int?> = _selectedAssigneeId.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _taskType = MutableStateFlow("task")
    val taskType: StateFlow<String> = _taskType.asStateFlow()

    private val _priority = MutableStateFlow("medium")
    val priority: StateFlow<String> = _priority.asStateFlow()

    private val _estimatedHours = MutableStateFlow("")
    val estimatedHours: StateFlow<String> = _estimatedHours.asStateFlow()

    private val _dueDate = MutableStateFlow("")
    val dueDate: StateFlow<String> = _dueDate.asStateFlow()

    private val _uiState = MutableStateFlow<CreateTaskState>(CreateTaskState.Idle)
    val uiState: StateFlow<CreateTaskState> = _uiState.asStateFlow()

    init {
        // Завантажуємо проєкти ЗАВЖДИ, щоб мати доступ до їхніх учасників (members)
        loadProjects()
    }

    private fun loadProjects() {
        viewModelScope.launch {
            val result = projectRepository.getProjects()
            result.onSuccess { projectList ->
                _projects.value = projectList
            }
        }
    }

    fun setTitle(newTitle: String) { _title.value = newTitle; clearError() }
    fun setDescription(newDesc: String) { _description.value = newDesc }
    fun setTaskType(newType: String) { _taskType.value = newType }
    fun setPriority(newPriority: String) { _priority.value = newPriority }

    fun setProject(projectId: Int) {
        _selectedProjectId.value = projectId
        _selectedAssigneeId.value = null // СКИДАЄМО виконавця, якщо змінили проєкт
        clearError()
    }

    fun setAssignee(userId: Int?) { _selectedAssigneeId.value = userId; clearError() }
    fun setEstimatedHours(hours: String) { _estimatedHours.value = hours }
    fun setDueDate(date: String) { _dueDate.value = date }

    fun submitTask() {
        val currentTitle = _title.value.trim()
        val currentProject = _selectedProjectId.value

        if (currentTitle.isBlank()) {
            _uiState.value = CreateTaskState.Error("Назва задачі є обов'язковою")
            return
        }

        if (currentProject == null || currentProject == 0) {
            _uiState.value = CreateTaskState.Error("Будь ласка, оберіть проєкт")
            return
        }

        viewModelScope.launch {
            _uiState.value = CreateTaskState.Loading

            val formattedDate = _dueDate.value.trim().takeIf { it.isNotEmpty() }?.let {
                if (it.length == 10) "${it}T12:00:00Z" else it
            }

            val request = CreateTaskRequest(
                title = currentTitle,
                description = _description.value.trim().takeIf { it.isNotEmpty() },
                project = currentProject,
                taskType = _taskType.value,
                priority = _priority.value,
                status = "to_do",
                assignee = _selectedAssigneeId.value,
                estimatedHours = _estimatedHours.value.replace(",", ".").toFloatOrNull(),
                dueDate = formattedDate
            )

            val result = repository.createTask(request)

            result.onSuccess {
                _uiState.value = CreateTaskState.Success
            }.onFailure { error ->
                _uiState.value = CreateTaskState.Error(error.message ?: "Сталася помилка")
            }
        }
    }

    private fun clearError() {
        if (_uiState.value is CreateTaskState.Error) {
            _uiState.value = CreateTaskState.Idle
        }
    }
}
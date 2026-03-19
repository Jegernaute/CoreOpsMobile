package com.example.coreops.ui.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coreops.data.remote.models.ProjectDto
import com.example.coreops.domain.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Описуємо всі можливі стани екрану проєктів.
 */
sealed interface ProjectsState {
    object Loading : ProjectsState
    data class Success(val projects: List<ProjectDto>) : ProjectsState
    data class Error(val message: String) : ProjectsState
}

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val repository: ProjectRepository // Hilt сам підставить сюди ProjectRepositoryImpl
) : ViewModel() {

    // Внутрішній стан (який можна змінювати)
    private val _state = MutableStateFlow<ProjectsState>(ProjectsState.Loading)
    // Зовнішній стан для UI (тільки для читання)
    val state: StateFlow<ProjectsState> = _state.asStateFlow()

    init {
        // Одразу при відкритті екрану запускає завантаження
        loadProjects()
    }

    fun loadProjects() {
        // viewModelScope означає що запит скасується автоматично якщо користувач закриє екран
        viewModelScope.launch {
            _state.value = ProjectsState.Loading

            // Звертаємося до репозиторію
            val result = repository.getProjects()

            // Обробляє результат за допомогою зручної функції fold
            result.fold(
                onSuccess = { projectsList ->
                    _state.value = ProjectsState.Success(projectsList)
                },
                onFailure = { exception ->
                    _state.value = ProjectsState.Error(
                        message = exception.message ?: "Сталася невідома помилка при завантаженні проєктів"
                    )
                }
            )
        }
    }
}
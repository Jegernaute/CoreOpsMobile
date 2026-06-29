package com.example.coreops.ui.projects

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coreops.data.remote.models.ProjectDto
import com.example.coreops.domain.TaskSyncManager
import com.example.coreops.domain.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProjectsState {
    object Loading : ProjectsState
    data class Success(val projects: List<ProjectDto>, val hasMore: Boolean = false) : ProjectsState
    data class Error(val message: String) : ProjectsState
}

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val repository: ProjectRepository,
    private val syncManager: TaskSyncManager // 1. Інжектимо твій менеджер синхронізації
) : ViewModel() {

    private val _state = MutableStateFlow<ProjectsState>(ProjectsState.Loading)
    val state: StateFlow<ProjectsState> = _state.asStateFlow()

    private var nextCursor: String? = null
    private var isLoadingMore = false

    init {
        // Початкове завантаження (із лоадером)
        loadProjects()

        // 2. Починаємо слухати глобальні оновлення задач
        viewModelScope.launch {
            syncManager.serverFetchRequests.collect {
                // Щойно якась задача на іншому екрані змінилася і смикнула сервер,
                // ми робимо "тихе" оновлення списку проєктів
                loadProjects(isSilent = true)
            }
        }
    }

    // 3. Додано прапорець isSilent (за замовчуванням false)
    fun loadProjects(isSilent: Boolean = false) {
        viewModelScope.launch {
            // Показуємо екран завантаження ТІЛЬКИ якщо це не тихе оновлення
            if (!isSilent) {
                _state.value = ProjectsState.Loading
            }
            nextCursor = null

            val result = repository.getProjects(cursor = null)

            result.fold(
                onSuccess = { paginatedResponse ->
                    nextCursor = extractCursor(paginatedResponse.next)

                    // Оновлюємо дані. Якщо це було тихе оновлення, UI просто
                    // миттєво "перемалює" нові відсотки та цифри без стрибків
                    _state.value = ProjectsState.Success(
                        projects = paginatedResponse.results,
                        hasMore = nextCursor != null
                    )
                },
                onFailure = { exception ->
                    if (!isSilent) {
                        _state.value = ProjectsState.Error(
                            message = exception.message ?: "Сталася невідома помилка при завантаженні проєктів"
                        )
                    }
                    // Якщо помилка сталася під час "тихого" оновлення у фоні,
                    // ми її просто ігноруємо, щоб не ламати поточний вигляд екрану користувачу
                }
            )
        }
    }

    fun loadMoreProjects() {
        if (isLoadingMore || nextCursor == null) return

        val currentState = _state.value
        if (currentState !is ProjectsState.Success) return

        isLoadingMore = true
        viewModelScope.launch {
            repository.getProjects(cursor = nextCursor).fold(
                onSuccess = { paginatedResponse ->
                    val accumulatedProjects = currentState.projects + paginatedResponse.results
                    nextCursor = extractCursor(paginatedResponse.next)

                    _state.value = ProjectsState.Success(
                        projects = accumulatedProjects,
                        hasMore = nextCursor != null
                    )
                    isLoadingMore = false
                },
                onFailure = {
                    isLoadingMore = false
                }
            )
        }
    }

    private fun extractCursor(url: String?): String? {
        if (url == null) return null
        return try {
            Uri.parse(url).getQueryParameter("cursor")
        } catch (e: Exception) {
            null
        }
    }
}
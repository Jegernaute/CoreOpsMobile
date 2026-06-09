package com.example.coreops.ui.projects

import android.net.Uri //
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
    // Додано hasMore для підтримки підвантаження нових сторінок
    data class Success(val projects: List<ProjectDto>, val hasMore: Boolean = false) : ProjectsState
    data class Error(val message: String) : ProjectsState
}

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val repository: ProjectRepository
) : ViewModel() {

    // Внутрішній стан (який можна змінювати)
    private val _state = MutableStateFlow<ProjectsState>(ProjectsState.Loading)
    // Зовнішній стан для UI (тільки для читання)
    val state: StateFlow<ProjectsState> = _state.asStateFlow()

    // Змінні для керування станом пагінації
    private var nextCursor: String? = null
    private var isLoadingMore = false

    init {
        // Одразу при відкритті екрану запускає завантаження
        loadProjects()
    }

    // Первинне завантаження або оновлення (Pull-to-Refresh)
    fun loadProjects() {
        viewModelScope.launch {
            _state.value = ProjectsState.Loading
            nextCursor = null

            // Звертається до репозиторію
            val result = repository.getProjects(cursor = null)

            // Обробляє результат за допомогою  функції fold
            result.fold(
                onSuccess = { paginatedResponse ->
                    nextCursor = extractCursor(paginatedResponse.next)
                    _state.value = ProjectsState.Success(
                        projects = paginatedResponse.results,
                        hasMore = nextCursor != null
                    )
                },
                onFailure = { exception ->
                    _state.value = ProjectsState.Error(
                        message = exception.message ?: "Сталася невідома помилка при завантаженні проєктів"
                    )
                }
            )
        }
    }

    // Підвантаження наступної сторінки проєктів при скролі вниз
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

    // Допоміжна функція для вирізання токена курсора з рядка URL
    private fun extractCursor(url: String?): String? {
        if (url == null) return null
        return try {
            Uri.parse(url).getQueryParameter("cursor")
        } catch (e: Exception) {
            null
        }
    }
}
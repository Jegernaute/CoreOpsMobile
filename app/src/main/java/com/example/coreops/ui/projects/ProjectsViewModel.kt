package com.example.coreops.ui.projects

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coreops.data.remote.models.ProjectDto
import com.example.coreops.domain.TaskSyncManager
import com.example.coreops.domain.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.coreops.domain.repository.ProfileRepository

sealed interface ProjectsState {
    object Loading : ProjectsState
    data class Success(val projects: List<ProjectDto>, val hasMore: Boolean = false) : ProjectsState
    data class Error(val message: String) : ProjectsState
}

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val repository: ProjectRepository,
    private val profileRepository: ProfileRepository,
    private val syncManager: TaskSyncManager
) : ViewModel() {

    private val _state = MutableStateFlow<ProjectsState>(ProjectsState.Loading)
    val state: StateFlow<ProjectsState> = _state.asStateFlow()

    // Стан для аватара користувача
    private val _avatarUrl = MutableStateFlow<String?>(null)
    val avatarUrl: StateFlow<String?> = _avatarUrl.asStateFlow()

    // Стан для тексту пошуку
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Стан для сортування (за замовчуванням null)
    private val _ordering = MutableStateFlow<String?>(null)
    val ordering: StateFlow<String?> = _ordering.asStateFlow()

    // Стан для відображення архівованих проєктів
    private val _showArchived = MutableStateFlow(false)
    val showArchived: StateFlow<Boolean> = _showArchived.asStateFlow()

    // Додавання станів фільтрації
    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status.asStateFlow()

    private val _hasActiveTasks = MutableStateFlow<Boolean?>(null)
    val hasActiveTasks: StateFlow<Boolean?> = _hasActiveTasks.asStateFlow()

    private val _isCompleted = MutableStateFlow<Boolean?>(null)
    val isCompleted: StateFlow<Boolean?> = _isCompleted.asStateFlow()

    // Оновлення параметрів фільтрації (BottomSheet)
    fun updateFilters(newOrdering: String?, includeArchived: Boolean, activeTasksOnly: Boolean?, completedOnly: Boolean?) {
        _ordering.value = newOrdering
        _showArchived.value = includeArchived
        _hasActiveTasks.value = activeTasksOnly
        _isCompleted.value = completedOnly
        loadProjects()
    }

    // Оновлення статусу (Чіпи)
    fun updateStatus(newStatus: String?) {
        _status.value = newStatus
        loadProjects()
    }
    private var nextCursor: String? = null
    private var isLoadingMore = false
    private var searchJob: Job? = null

    init {
        // Початкове завантаження (із лоадером)
        loadProjects()
        loadUserProfile()

        // 2. Починає слухати глобальні оновлення задач
        viewModelScope.launch {
            syncManager.serverFetchRequests.collect {
                // Щойно якась задача на іншому екрані змінилася і смикнула сервер,
                //  робиться "тихе" оновлення списку проєктів
                loadProjects(isSilent = true)
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            profileRepository.getMyProfile().onSuccess { userDto ->
                _avatarUrl.value = userDto.safeAvatarUrl
            }
        }
    }

    // Обробка зміни тексту в полі пошуку
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel() // Скасовує попередній таймер
        searchJob = viewModelScope.launch {
            delay(300) // Чекає 300мс після останнього натискання клавіші
            loadProjects()
        }
    }

    // 3. Додано прапорець isSilent (за замовчуванням false)
    fun loadProjects(isSilent: Boolean = false) {
        viewModelScope.launch {
            // Показує екран завантаження ТІЛЬКИ якщо це не тихе оновлення
            if (!isSilent) {
                _state.value = ProjectsState.Loading
            }
            nextCursor = null

            val currentQuery = _searchQuery.value.takeIf { it.isNotBlank() }
            val currentOrdering = _ordering.value
            val currentShowArchived = _showArchived.value.takeIf { it } // передача true або null

            val currentStatus = _status.value
            val currentHasActiveTasks = _hasActiveTasks.value
            val currentIsCompleted = _isCompleted.value

            val result = repository.getProjects(
                cursor = null,
                search = currentQuery,
                ordering = currentOrdering,
                showArchived = currentShowArchived,
                status = currentStatus,
                hasActiveTasks = currentHasActiveTasks,
                isCompleted = currentIsCompleted
            )

            result.fold(
                onSuccess = { paginatedResponse ->
                    nextCursor = extractCursor(paginatedResponse.next)

                    // Оновлює дані. Якщо це було тихе оновлення, UI просто
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
                    // її просто ігнорує, щоб не ламати поточний вигляд екрану користувачу
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
            val currentQuery = _searchQuery.value.takeIf { it.isNotBlank() }
            val currentOrdering = _ordering.value
            val currentShowArchived = _showArchived.value.takeIf { it }
            val currentStatus = _status.value
            val currentHasActiveTasks = _hasActiveTasks.value
            val currentIsCompleted = _isCompleted.value

            repository.getProjects(
                cursor = nextCursor,
                search = currentQuery,
                ordering = currentOrdering,
                showArchived = currentShowArchived,
                status = currentStatus,
                hasActiveTasks = currentHasActiveTasks,
                isCompleted = currentIsCompleted
            ).fold(
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
package com.example.coreops.ui.notifications

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coreops.data.remote.models.NotificationDto
import com.example.coreops.domain.TaskSyncManager
import com.example.coreops.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Стан екрану сповіщень
sealed class NotificationState {
    object Loading : NotificationState()
    data class Success(val notifications: List<NotificationDto>, val hasMore: Boolean = false) : NotificationState()
    data class Error(val message: String) : NotificationState()
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationRepository,
    private val syncManager: TaskSyncManager
) : ViewModel() {

    private val _state = MutableStateFlow<NotificationState>(NotificationState.Loading)
    val state: StateFlow<NotificationState> = _state.asStateFlow()

    private var nextCursor: String? = null
    private var isLoadingMore = false

    init {
        loadNotifications()

        viewModelScope.launch {
            syncManager.serverFetchRequests.collect {
                delay(2000)
                loadNotifications()
            }
        }
    }

    // Первинне завантаження або повне оновлення списку (Pull-to-Refresh)
    fun loadNotifications() {
        viewModelScope.launch {
            _state.value = NotificationState.Loading
            nextCursor = null

            repository.getNotifications(cursor = null).onSuccess { paginatedResponse ->
                nextCursor = extractCursor(paginatedResponse.next)
                _state.value = NotificationState.Success(
                    notifications = paginatedResponse.results,
                    hasMore = nextCursor != null
                )
            }.onFailure { error ->
                _state.value = NotificationState.Error(error.message ?: "Помилка завантаження")
            }
        }
    }

    // Підвантаження наступної сторінки сповіщень при скролі вниз
    fun loadMoreNotifications() {
        if (isLoadingMore || nextCursor == null) return

        val currentState = _state.value
        if (currentState !is NotificationState.Success) return

        isLoadingMore = true
        viewModelScope.launch {
            repository.getNotifications(cursor = nextCursor).onSuccess { paginatedResponse ->
                val accumulatedNotifications = currentState.notifications + paginatedResponse.results
                nextCursor = extractCursor(paginatedResponse.next)

                _state.value = NotificationState.Success(
                    notifications = accumulatedNotifications,
                    hasMore = nextCursor != null
                )
                isLoadingMore = false
            }.onFailure {
                isLoadingMore = false
            }
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

    /**
     * Позначає одне сповіщення як прочитане
     */
    fun markAsRead(id: Int) {
        viewModelScope.launch {
            val result = repository.markAsRead(id)

            result.onSuccess {
                val currentState = _state.value
                if (currentState is NotificationState.Success) {
                    val updatedList = currentState.notifications.map { notif ->
                        if (notif.id == id) notif.copy(isRead = true) else notif
                    }
                    _state.value = currentState.copy(notifications = updatedList)
                }
            }
        }
    }

    /**
     * Позначає всі сповіщення як прочитані
     */
    fun markAllAsRead() {
        viewModelScope.launch {
            val result = repository.markAllAsRead()

            result.onSuccess {
                val currentState = _state.value
                if (currentState is NotificationState.Success) {
                    val updatedList = currentState.notifications.map { notif ->
                        notif.copy(isRead = true)
                    }
                    _state.value = currentState.copy(notifications = updatedList)
                }
            }
        }
    }
}
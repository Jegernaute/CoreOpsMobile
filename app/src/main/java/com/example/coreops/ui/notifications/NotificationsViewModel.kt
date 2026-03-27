package com.example.coreops.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coreops.data.remote.models.NotificationDto
import com.example.coreops.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.coreops.domain.TaskSyncManager

// Стан екрану сповіщень
sealed class NotificationState {
    object Loading : NotificationState()
    data class Success(val notifications: List<NotificationDto>) : NotificationState()
    data class Error(val message: String) : NotificationState()
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationRepository,
    private val syncManager: TaskSyncManager
) : ViewModel() {

    private val _state = MutableStateFlow<NotificationState>(NotificationState.Loading)
    val state: StateFlow<NotificationState> = _state.asStateFlow()

    init {
        loadNotifications()

        viewModelScope.launch {
            syncManager.serverUpdates.collect {
                kotlinx.coroutines.delay(2000)
                loadNotifications()
            }
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            if (_state.value !is NotificationState.Success) {
                _state.value = NotificationState.Loading
            }

            val result = repository.getNotifications()

            result.onSuccess { notifications ->
                _state.value = NotificationState.Success(notifications)
            }
            result.onFailure { error ->

                if (_state.value !is NotificationState.Success) {
                    _state.value = NotificationState.Error(error.message ?: "Помилка завантаження")
                }
            }
        }
    }

    /**
     * Позначає одне сповіщення як прочитане
     */
    fun markAsRead(id: Int) {
        viewModelScope.launch {
            val result = repository.markAsRead(id)

            result.onSuccess {
                // Локально оновлюємо список: шукаємо потрібне і міняємо isRead на true
                val currentState = _state.value
                if (currentState is NotificationState.Success) {
                    val updatedList = currentState.notifications.map { notif ->
                        if (notif.id == id) notif.copy(isRead = true) else notif
                    }
                    _state.value = NotificationState.Success(updatedList)
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
                // Локально міняємо всім isRead на true
                val currentState = _state.value
                if (currentState is NotificationState.Success) {
                    val updatedList = currentState.notifications.map { notif ->
                        notif.copy(isRead = true)
                    }
                    _state.value = NotificationState.Success(updatedList)
                }
            }
        }
    }
}
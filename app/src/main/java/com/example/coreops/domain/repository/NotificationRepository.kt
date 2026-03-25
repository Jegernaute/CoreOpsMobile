package com.example.coreops.domain.repository

import com.example.coreops.data.remote.models.NotificationDto

interface NotificationRepository {
    suspend fun getNotifications(): Result<List<NotificationDto>>
    suspend fun markAsRead(notificationId: Int): Result<Unit>
    suspend fun markAllAsRead(): Result<Unit>
}
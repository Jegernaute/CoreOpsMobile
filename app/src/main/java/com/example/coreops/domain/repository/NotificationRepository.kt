package com.example.coreops.domain.repository

import com.example.coreops.data.remote.models.NotificationDto
import com.example.coreops.data.remote.models.PaginatedResponse

interface NotificationRepository {
    suspend fun getNotifications(cursor: String? = null): Result<PaginatedResponse<NotificationDto>>
    suspend fun markAsRead(notificationId: Int): Result<Unit>
    suspend fun markAllAsRead(): Result<Unit>
}
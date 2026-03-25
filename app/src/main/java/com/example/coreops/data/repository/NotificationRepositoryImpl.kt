package com.example.coreops.data.repository

import com.example.coreops.data.remote.api.NotificationsApi
import com.example.coreops.data.remote.models.NotificationDto
import com.example.coreops.domain.repository.NotificationRepository
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val api: NotificationsApi
) : NotificationRepository {

    override suspend fun getNotifications(): Result<List<NotificationDto>> {
        return try {
            val response = api.getNotifications()
            Result.success(response.results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAsRead(notificationId: Int): Result<Unit> {
        return try {
            api.markAsRead(notificationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAllAsRead(): Result<Unit> {
        return try {
            api.markAllAsRead()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
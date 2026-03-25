package com.example.coreops.data.remote.api

import com.example.coreops.data.remote.models.NotificationDto
import com.example.coreops.data.remote.models.PaginatedResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationsApi {

    /**
     * Отримання списку сповіщень поточного користувача.
     * Django використовує StandardResultsSetPagination, тому обгортаєтся в PaginatedResponse.
     */
    @GET("api/v1/notifications/")
    suspend fun getNotifications(): PaginatedResponse<NotificationDto>

    /**
     * Помітити конкретне сповіщення як прочитане.
     */
    @POST("api/v1/notifications/{id}/mark_read/")
    suspend fun markAsRead(@Path("id") notificationId: Int)

    /**
     * Помітити всі сповіщення як прочитані.
     */
    @POST("api/v1/notifications/mark_all_read/")
    suspend fun markAllAsRead()
}
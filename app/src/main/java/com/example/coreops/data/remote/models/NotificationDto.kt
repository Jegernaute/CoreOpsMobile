package com.example.coreops.data.remote.models

import com.google.gson.annotations.SerializedName

data class NotificationDto(
    val id: Int,
    @SerializedName("notification_type")
    val notificationType: String,
    val title: String,
    val message: String,
    @SerializedName("target_url")
    val targetUrl: String?,
    @SerializedName("is_read")
    val isRead: Boolean,
    @SerializedName("created_at")
    val createdAt: String
)
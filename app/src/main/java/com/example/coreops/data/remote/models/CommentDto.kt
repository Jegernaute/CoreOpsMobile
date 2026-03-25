package com.example.coreops.data.remote.models

import com.google.gson.annotations.SerializedName

data class CommentDto(
    val id: Int,
    @SerializedName("task")
    val taskId: Int,
    @SerializedName("author")
    val authorId: Int?,
    @SerializedName("author_name")
    val authorName: String?,
    @SerializedName("author_avatar")
    val authorAvatar: String?,
    val content: String,
    @SerializedName("created_at")
    val createdAt: String
)
package com.example.coreops.data.remote.models

import com.google.gson.annotations.SerializedName

data class UserDto(
    val id: Int,
    val email: String,

    @SerializedName("full_name")
    val fullName: String,

    val avatar: String?,

    @SerializedName("job_title")
    val jobTitle: String?,

    val phone: String?,
    val telegram: String?
)
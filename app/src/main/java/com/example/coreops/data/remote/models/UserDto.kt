package com.example.coreops.data.remote.models

import com.google.gson.annotations.SerializedName

data class UserDto(
    val id: Int,
    val email: String,

    @SerializedName("first_name")
    val firstName: String,

    @SerializedName("last_name")
    val lastName: String,

    val avatar: String?,

    @SerializedName("job_title")
    val jobTitle: String?,

    val phone: String?,
    val telegram: String?,

    @SerializedName("global_role")
    val globalRole: String
) {
    // Зручна властивість для відображення в UI
    val fullName: String
        get() = "$firstName $lastName".trim()
}
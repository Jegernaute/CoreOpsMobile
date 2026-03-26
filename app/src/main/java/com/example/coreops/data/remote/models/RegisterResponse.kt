package com.example.coreops.data.remote.models

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    val id: Int,
    val email: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String
)
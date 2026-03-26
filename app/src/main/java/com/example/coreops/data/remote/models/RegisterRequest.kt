package com.example.coreops.data.remote.models

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val token: String,
    val password: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String
)
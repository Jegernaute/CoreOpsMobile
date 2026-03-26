package com.example.coreops.data.remote.models

data class TokenRefreshResponse(
    val access: String,
    val refresh: String? = null
)
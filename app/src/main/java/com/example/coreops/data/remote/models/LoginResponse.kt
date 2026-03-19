package com.example.coreops.data.remote.models

import com.google.gson.annotations.SerializedName

/**
 * Модель даних для отримання JWT-токенів від сервера.
 * @SerializedName вказує Gson, як саме називається поле у JSON-відповіді.
 */
data class LoginResponse(
    @SerializedName("access") val access: String,
    @SerializedName("refresh") val refresh: String
)
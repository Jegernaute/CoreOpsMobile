package com.example.coreops.data.remote.models

/**
 * Модель даних для відправки логіну та паролю на сервер.
 */
data class LoginRequest(
    val email: String,
    val password: String
)
package com.example.coreops.data.remote.api

import com.example.coreops.data.remote.models.LoginRequest
import com.example.coreops.data.remote.models.LoginResponse
import com.example.coreops.data.remote.models.RegisterRequest
import com.example.coreops.data.remote.models.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Інтерфейс Retrofit для зв'язку з бекендом.
 * Виконує POST-запит для отримання токенів авторизації.
 */
interface AuthApi {

    @POST("api/v1/users/token/")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/v1/users/register/")
    suspend fun register(
        @Body request: RegisterRequest
    ): RegisterResponse

}
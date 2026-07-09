package com.example.coreops.data.remote.api

import com.example.coreops.data.remote.models.UserDto
import retrofit2.http.GET

interface UsersApi {
    @GET("api/v1/users/me/")
    suspend fun getMyProfile(): UserDto
}
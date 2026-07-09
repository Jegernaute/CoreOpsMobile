package com.example.coreops.domain.repository

import com.example.coreops.data.remote.api.UsersApi
import com.example.coreops.data.remote.models.UserDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val api: UsersApi
) {
    suspend fun getMyProfile(): Result<UserDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getMyProfile()
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
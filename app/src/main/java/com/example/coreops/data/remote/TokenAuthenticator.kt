package com.example.coreops.data.remote

import com.example.coreops.data.local.AuthPreferences
import com.example.coreops.data.remote.api.AuthApi
import com.example.coreops.data.remote.models.TokenRefreshRequest
import dagger.Lazy
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val authApi: Lazy<AuthApi>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {

        if (response.priorResponse != null) {
            return null
        }

        if (response.request.header("Authorization") == null) {
            return null
        }

        // Синхронне читання
        val refreshToken = authPreferences.getRefreshToken()

        if (refreshToken.isNullOrBlank()) {
            return null
        }

        try {
            val refreshResponse = authApi.get().refreshToken(
                TokenRefreshRequest(refresh = refreshToken)
            ).execute()

            if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                val newAccessToken = refreshResponse.body()!!.access
                val newRefreshToken = refreshResponse.body()!!.refresh ?: refreshToken

                // Збереження миттєво оновить кеш, а запис на диск піде у фоні
                authPreferences.saveTokens(newAccessToken, newRefreshToken)

                return response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            } else {
                authPreferences.clearTokens()
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }
}
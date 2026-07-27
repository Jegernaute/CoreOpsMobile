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
import javax.inject.Singleton
@Singleton
class TokenAuthenticator @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val authApi: Lazy<AuthApi>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {

        if (response.priorResponse != null) {
            return null
        }

        val originalHeader = response.request.header("Authorization")
        if (originalHeader == null) {
            return null
        }

        // Блокує доступ для інших потоків
        synchronized(this) {
            // ПОДВІЙНА ПЕРЕВІРКА:
            // Поки цей потік чекає своєї черги, можливо інший потік вже успішно оновив токен
            val currentToken = authPreferences.getAccessToken()
            val originalTokenFromRequest = originalHeader.removePrefix("Bearer ")

            if (currentToken != null && currentToken != originalTokenFromRequest) {
                // Токен вже новий. Просто повторить запит із цим новим токеном.
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            // Якщо токени однакові, значить його ще ніхто не оновив. Робить рефреш
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
}
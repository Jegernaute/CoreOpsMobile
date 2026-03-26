package com.example.coreops.data.remote

import com.example.coreops.data.local.AuthPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Перехоплювач мережевих запитів.
 * Призначений для автоматичного підставлення токена авторизації у заголовок запиту.
 */
class AuthInterceptor @Inject constructor(
    private val authPreferences: AuthPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
        val path = request.url.encodedPath

        //Пропускає запити логіну та реєстрації БЕЗ додавання токену
        if (path.contains("/auth/jwt/create/") || path.contains("/register/")) {
            return chain.proceed(request)
        }

        // Отримання токена синхронно за допомогою runBlocking,
        // оскільки Interceptor працює у фоновому потоці і це не заблокує UI.
        val token = runBlocking { authPreferences.getAccessToken().first() }

        val requestBuilder = chain.request().newBuilder()

        // Додавання заголовка Authorization, якщо токен існує у сховищі
        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}
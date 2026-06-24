package com.example.coreops.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.inject.Inject

/**
 * AuthPreferences — безпечне сховище для токенів та облікових даних.
 * Використовує In-Memory кешування для миттєвого доступу без блокування потоків (I/O).
 */
class AuthPreferences @Inject constructor(context: Context) {

    private val prefs: SharedPreferences

    // In-Memory кеш для уникнення постійного читання з файлової системи
    private var cachedAccessToken: String? = null
    private var cachedRefreshToken: String? = null

    init {
        // Створює майстер-ключ для апаратного шифрування
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context,
            "auth_encrypted_preferences",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        // Зчитує дані з диска ЛИШЕ ОДИН РАЗ при старті додатка
        cachedAccessToken = prefs.getString("access_token", null)
        cachedRefreshToken = prefs.getString("refresh_token", null)
    }

    // --- СИНХРОННІ МЕТОДИ ДЛЯ ТОКЕНІВ ---

    fun getAccessToken(): String? = cachedAccessToken

    fun getRefreshToken(): String? = cachedRefreshToken

    fun saveTokens(access: String, refresh: String) {
        cachedAccessToken = access
        cachedRefreshToken = refresh

        // apply() виконує запис на диск асинхронно у фоні
        prefs.edit()
            .putString("access_token", access)
            .putString("refresh_token", refresh)
            .apply()
    }

    fun clearTokens() {
        cachedAccessToken = null
        cachedRefreshToken = null
        prefs.edit()
            .remove("access_token")
            .remove("refresh_token")
            .remove("saved_email")
            .remove("saved_password")
            .apply()
    }

    // --- МЕТОДИ ДЛЯ ДАНИХ АВТОРИЗАЦІЇ ---

    fun getSavedEmail(): String? = prefs.getString("saved_email", null)

    fun getSavedPassword(): String? = prefs.getString("saved_password", null)

    fun saveCredentials(email: String, password: String) {
        prefs.edit()
            .putString("saved_email", email)
            .putString("saved_password", password)
            .apply()
    }

    fun clearCredentials() {
        prefs.edit()
            .remove("saved_email")
            .remove("saved_password")
            .apply()
    }
}
package com.example.coreops.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * AuthPreferences - це клас, який є обгорткою над DataStore.
 * Його єдина відповідальність (Single Responsibility) — безпечно зберігати,
 * читати та видаляти JWT-токени (access та refresh), які потрібні для доступу до API.
 */
class AuthPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    /**
     * Об'єкт Companion містить константи — ключі, за якими шукаються дані.
     * stringPreferencesKey створює типізований ключ спеціально для рядків (String).
     */
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        val SAVED_EMAIL_KEY = stringPreferencesKey("saved_email")
        val SAVED_PASSWORD_KEY = stringPreferencesKey("saved_password")
    }

    /**
     * Метод для збереження обох токенів після успішної авторизації.
     * @param access - короткостроковий токен для запитів.
     * @param refresh - довгостроковий токен для оновлення access-токена.
     * Використовуємо `suspend`, оскільки запис у файл — це асинхронна операція.
     */
    suspend fun saveTokens(access: String, refresh: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = access
            preferences[REFRESH_TOKEN_KEY] = refresh
        }
    }

    /**
     * Повертає потік (Flow) даних з Access токеном.
     * Flow дозволяє нам реагувати на зміни: якщо токен оновиться,
     * всі підписники цього Flow автоматично отримають нове значення.
     */
    fun getAccessToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN_KEY]
        }
    }

    /**
     * Повертає потік з Refresh токеном.
     */
    fun getRefreshToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN_KEY]
        }
    }

    /**
     * Очищає всі токени з DataStore.
     * Цей метод викликається при виході з акаунта (Logout)
     * або якщо refresh-токен прострочився (Session Expired).
     */
    suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            // Якщо треба видаляти дані для входу розкоментувати:
             preferences.remove(SAVED_EMAIL_KEY)
             preferences.remove(SAVED_PASSWORD_KEY)
        }
    }

    // Читання збереженого email
    val savedEmail: Flow<String?> = dataStore.data.map { preferences ->
        preferences[SAVED_EMAIL_KEY]
    }

    // Читання збереженого пароля
    val savedPassword: Flow<String?> = dataStore.data.map { preferences ->
        preferences[SAVED_PASSWORD_KEY]
    }

    // Збереження email та пароля одночасно
    suspend fun saveCredentials(email: String, password: String) {
        dataStore.edit { preferences ->
            preferences[SAVED_EMAIL_KEY] = email
            preferences[SAVED_PASSWORD_KEY] = password
        }
    }

    // Функція для очищення саме кредосів (наприклад, якщо користувач зняв галочку "Запам'ятати мене")
    suspend fun clearCredentials() {
        dataStore.edit { preferences ->
            preferences.remove(SAVED_EMAIL_KEY)
            preferences.remove(SAVED_PASSWORD_KEY)
        }
    }
}

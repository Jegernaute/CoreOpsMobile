package com.example.coreops.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coreops.data.local.AuthPreferences
import com.example.coreops.data.remote.api.AuthApi
import com.example.coreops.data.remote.models.LoginRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * Класи станів для екрану авторизації.
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val api: AuthApi,
    private val authPreferences: AuthPreferences
) : ViewModel() {

    // Внутрішній змінний стан
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    // Публічний стан тільки для читання (для UI)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    // Стан для чекбоксу "Запам'ятати мене"
    private val _rememberMe = MutableStateFlow(false)
    val rememberMe: StateFlow<Boolean> = _rememberMe.asStateFlow()

    init {
        // При створенні ViewModel одразу йде в сейф і дістає кредоси
        viewModelScope.launch {
            // firstOrNull() бере значення один раз і зупиняється (не слухає вічно)
            val savedEmail = authPreferences.savedEmail.firstOrNull()
            val savedPassword = authPreferences.savedPassword.firstOrNull()

            if (!savedEmail.isNullOrBlank()) {
                _email.value = savedEmail
                _rememberMe.value = true
            }
            if (!savedPassword.isNullOrBlank()) {
                _password.value = savedPassword
            }
        }
    }

    fun onRememberMeChange(isChecked: Boolean) {
        _rememberMe.value = isChecked
    }

    // Функції для оновлення тексту з UI
    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }


    fun login() {
        // Бере поточні значення зі StateFlow
        val currentEmail = _email.value
        val currentPassword = _password.value

        // Базова перевірка щоб поля були заповнені
        if (currentEmail.isBlank() || currentPassword.isBlank()) {
            _authState.value = AuthState.Error("Будь ласка, заповніть всі поля")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                // Передає змінні у запит
                val request = LoginRequest(currentEmail, currentPassword)
                val response = api.login(request)

                if (response.isSuccessful && response.body() != null) {
                    val tokens = response.body()!!
                    // Збереження токенів
                    authPreferences.saveTokens(tokens.access, tokens.refresh)

                    // Логіка Запам'ятати мене
                    if (_rememberMe.value) {
                        authPreferences.saveCredentials(currentEmail, currentPassword)
                    } else {
                        // Якщо галочка знята стираються старі збережені дані
                        authPreferences.clearCredentials()
                    }

                    _authState.value = AuthState.Success
                }
            } catch (e: HttpException) {
                _authState.value = AuthState.Error("Помилка сервера: ${e.code()}")
            } catch (e: IOException) {
                _authState.value = AuthState.Error("Помилка підключення до інтернету")
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Невідома помилка: ${e.localizedMessage}")
            }
        }
    }

    // --- Стан для екрану реєстрації ---
    private val _registerState = MutableStateFlow<AuthState>(AuthState.Idle)
    val registerState: StateFlow<AuthState> = _registerState.asStateFlow()

    /**
     * Відправляє дані нового користувача на сервер.
     */
    fun register(request: com.example.coreops.data.remote.models.RegisterRequest) {
        viewModelScope.launch {
            _registerState.value = AuthState.Loading

            try {
                // Відправляє запит
                api.register(request)

                // Якщо не вилетіло Exception (код 200/201) значить реєстрація успішна
                _registerState.value = AuthState.Success

            } catch (e: HttpException) {
                // Сервер повернув помилку (4xx або 5xx).
                if (e.code() == 400) {
                    _registerState.value = AuthState.Error("Помилка: Невірні дані або користувач з таким Email вже існує")
                } else {
                    _registerState.value = AuthState.Error("Помилка сервера: ${e.code()}")
                }
            } catch (e: IOException) {
                _registerState.value = AuthState.Error("Помилка підключення до інтернету")
            } catch (e: Exception) {
                _registerState.value = AuthState.Error("Невідома помилка: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Скидає стан реєстрації.
     * Корисно, коли юзер закриває вікно помилки або повертається на екран логіну.
     */
    fun resetRegisterState() {
        _registerState.value = AuthState.Idle
    }

    /**
     * Виконує вихід з акаунту: очищає локальні токени та скидає стан.
     */
    fun logout() {
        viewModelScope.launch {
            // 1. Очищає токени та збережені паролі в DataStore (фізична пам'ять)
            authPreferences.clearTokens()

            // 2. Очищає оперативну пам'ять ViewModel (щоб UI відразу оновився)
            _email.value = ""
            _password.value = ""
            _rememberMe.value = false

            // 3. Скидає стан авторизації до початкового
            _authState.value = AuthState.Idle
        }
    }
}
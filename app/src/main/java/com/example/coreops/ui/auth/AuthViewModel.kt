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

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                val request = LoginRequest(email, password)
                val response = api.login(request)

                if (response.isSuccessful && response.body() != null) {
                    val tokens = response.body()!!
                    // Збереження токенів при успішній авторизації
                    authPreferences.saveTokens(tokens.access, tokens.refresh)
                    _authState.value = AuthState.Success
                } else {
                    // Обробка помилок клієнта (наприклад, 400 або 401)
                    _authState.value = AuthState.Error("Невірний email або пароль")
                }
            } catch (e: HttpException) {
                // Помилки сервера (5xx)
                _authState.value = AuthState.Error("Помилка сервера: ${e.code()}")
            } catch (e: IOException) {
                // Помилки мережі (немає інтернету)
                _authState.value = AuthState.Error("Помилка підключення до інтернету")
            } catch (e: Exception) {
                // Інші непередбачені помилки
                _authState.value = AuthState.Error("Невідома помилка: ${e.localizedMessage}")
            }
        }
    }
}
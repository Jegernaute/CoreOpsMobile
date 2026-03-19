package com.example.coreops

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.coreops.ui.auth.LoginScreen
import com.example.coreops.ui.theme.CoreOpsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Обов'язкова анотація для Hilt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CoreOpsTheme {
                // Surface - це базовий контейнер екрану
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Викликаємо створений екран логіну
                    LoginScreen(
                        onLoginSuccess = {
                            // Поки що просто вводиться повідомлення в Logcat.
                            // Згодом тут буде перехід на головний екран додатку.
                            Log.d("MainActivity", "Авторизація успішна! Треба зробити навігацію.")
                        }
                    )
                }
            }
        }
    }
}
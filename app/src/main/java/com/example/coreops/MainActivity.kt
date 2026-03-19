package com.example.coreops

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.coreops.ui.navigation.RootNavGraph
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
                    // Створює глобальний контролер навігації
                    val navController = rememberNavController()

                    // Віддає управління графу
                    RootNavGraph(navController = navController)

                }
            }
        }
    }
}

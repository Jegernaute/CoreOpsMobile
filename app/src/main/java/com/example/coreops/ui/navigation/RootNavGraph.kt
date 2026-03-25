package com.example.coreops.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.coreops.ui.auth.LoginScreen
import com.example.coreops.ui.main.MainScreen

/**
 * Головний граф навігації всього додатка.
 * Керує переходом між екраном авторизації та основним робочим простором.
 */
@Composable
fun RootNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // --- 1. Екран Логіну ---
        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    // Переходить на головний екран
                    navController.navigate(Screen.Main.route) {
                        //  очищає історію навігації (бекстек) аж до екрану логіну включно
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // --- 2. Головний екран (контейнер з нижньою панеллю) ---
        composable(route = Screen.Main.route) {
            MainScreen(
                onLogout = {
                    // Безпечно переходить на екран логіну
                    navController.navigate(Screen.Login.route) {
                        // Знищує історію навігації щоб кнопка "Назад" на телефоні
                        // не повернула  назад у MainScreen після виходу
                        popUpTo(Screen.Main.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}
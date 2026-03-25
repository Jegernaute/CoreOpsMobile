package com.example.coreops.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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

        // --- Екран створення нової задачі ---
        composable(
            route = Screen.CreateTask.route,
            arguments = listOf(
                navArgument("projectId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            // Витягуємо ID проєкту з аргументів
            val projectId = backStackEntry.arguments?.getInt("projectId") ?: 0

            // Викликаємо екран створення (його ми створимо в наступній дії)
            com.example.coreops.ui.tasks.CreateTaskScreen(
                projectId = projectId,
                onNavigateBack = {
                    // Повертаємося назад при скасуванні
                    navController.popBackStack()
                },
                onTaskCreated = {
                    // Повертаємося на попередній екран після успішного створення
                    navController.popBackStack()
                }
            )
        }
    }
}
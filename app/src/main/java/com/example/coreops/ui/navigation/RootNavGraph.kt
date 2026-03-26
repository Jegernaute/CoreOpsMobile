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
        // --- Екран Логіну ---
        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { // <--- ДОДАЙ ОБРОБКУ КЛІКУ
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // --- Екран Реєстрації ---
        composable(route = Screen.Register.route) {
            com.example.coreops.ui.auth.RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    // Після успішної реєстрації повертає юзера на екран логіну
                    navController.popBackStack()
                }
            )
        }

        // --- Головний екран (контейнер з нижньою панеллю) ---
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
            // Витягує ID проєкту з аргументів
            val projectId = backStackEntry.arguments?.getInt("projectId") ?: 0

            // Викликає екран створення
            com.example.coreops.ui.tasks.CreateTaskScreen(
                projectId = projectId,
                onNavigateBack = {
                    // Повертає назад при скасуванні
                    navController.popBackStack()
                },
                onTaskCreated = {
                    // Повертає на попередній екран після успішного створення
                    navController.popBackStack()
                }
            )
        }
    }
}
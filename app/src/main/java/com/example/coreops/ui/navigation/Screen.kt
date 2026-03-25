package com.example.coreops.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Клас, що зберігає всі маршрути навігації в додатку.
 */
sealed class Screen(val route: String) {

    // --- Кореневі маршрути (на весь екран) ---
    object Login : Screen("login")

    // Main є контейнером для екранів з нижньою панеллю
    object Main : Screen("main")

    // --- Маршрути для нижньої панелі  ---
    sealed class BottomTab(
        route: String,
        val title: String,
        val icon: ImageVector
    ) : Screen(route) {

        object Projects : BottomTab(
            route = "projects",
            title = "Проєкти",
            icon = Icons.Outlined.Folder
        )

        object Notifications : BottomTab(
            route = "notifications",
            title = "Сповіщення",
            icon = Icons.Outlined.Notifications
        )

        object Profile : BottomTab(
            route = "profile",
            title = "Профіль",
            icon = Icons.Outlined.Person
        )
    }
    // --- Маршрути з параметрами (Вкладені екрани) ---

    // Маршрут-шаблон: "project_tasks/{projectId}"
    object ProjectTasks : Screen("project_tasks/{projectId}") {
        fun createRoute(projectId: Int): String {
            return "project_tasks/$projectId"
        }
    }

    // Маршрут-шаблон: "task_detail/{taskId}"
    object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: Int): String {
            return "task_detail/$taskId"
        }
    }

    // Маршрут для екрану створення задачі (приймає ID проєкту)
    object CreateTask : Screen("create_task/{projectId}") {
        fun createRoute(projectId: Int) = "create_task/$projectId"
    }
}
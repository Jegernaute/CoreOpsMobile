package com.example.coreops.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.coreops.ui.navigation.Screen
import com.example.coreops.ui.projects.ProjectsScreen

@Composable
fun MainScreen() {
    // Контролер для внутрішньої навігації (між вкладками)
    val navController = rememberNavController()

    // Отримуємо поточний маршрут, щоб знати, яка вкладка активна
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Список вкладок з класу Screen
    val bottomTabs = listOf(
        Screen.BottomTab.Projects,
        Screen.BottomTab.Notifications,
        Screen.BottomTab.Profile
    )

    // Scaffold - це базовий каркас екрану Material 3
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                bottomTabs.forEach { tab ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(text = tab.title) },
                        selected = currentRoute == tab.route, // Підсвічує, якщо маршрут збігається
                        onClick = {
                            navController.navigate(tab.route) {
                                // Налаштування для правильного бекстеку (щоб кнопка "Назад" працювала логічно)
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true // Уникає створення дублікатів екрану
                                restoreState = true // Зберігає стан вкладок при перемиканні
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF2563EB),
                            selectedTextColor = Color(0xFF2563EB),
                            unselectedIconColor = Color(0xFF6B7280),
                            unselectedTextColor = Color(0xFF6B7280),
                            indicatorColor = Color(0xFFEFF6FF)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        // NavHost відповідає за зміну контенту (екранів)
        NavHost(
            navController = navController,
            startDestination = Screen.BottomTab.Projects.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            //Екран Проєктів
            composable(Screen.BottomTab.Projects.route) {
                ProjectsScreen(
                    onProjectClick = { projectId ->
                        // Тут буде логіка переходу на "Деталі проєкту" в майбутньому.
                        println("Клік по проєкту з ID: $projectId")
                    }
                )
            }

            // Заглушка: Екран Сповіщень
            composable(Screen.BottomTab.Notifications.route) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Екран Сповіщень", fontSize = 24.sp, color = Color.Gray)
                }
            }

            // Заглушка: Екран Профілю
            composable(Screen.BottomTab.Profile.route) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Екран Профілю", fontSize = 24.sp, color = Color.Gray)
                }
            }
        }
    }
}
package com.example.coreops.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.coreops.ui.tasks.ProjectTasksScreen
import com.example.coreops.ui.tasks.TaskDetailScreen

@Composable
fun MainScreen(onLogout: () -> Unit) {
    // Контролер для внутрішньої навігації (між вкладками)
    val navController = rememberNavController()

    // Отримує поточний маршрут щоб знати яка вкладка активна
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
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
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
                        navController.navigate(Screen.ProjectTasks.createRoute(projectId))
                    }
                )
            }

            // Екран задач
            composable(
                route = Screen.ProjectTasks.route,
                arguments = listOf(
                    navArgument("projectId") {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getInt("projectId") ?: 0

                ProjectTasksScreen(
                    projectId = projectId,
                    onNavigateBack = { navController.popBackStack() },
                    onTaskClick = { taskId ->
                        navController.navigate(Screen.TaskDetail.createRoute(taskId))
                    },
                    onCreateTaskClick = {
                        navController.navigate(Screen.CreateTask.createRoute(projectId))
                    }
                )
            }

            // Екран деталей конкретної задачі
            composable(
                route = Screen.TaskDetail.route,
                arguments = listOf(
                    navArgument("taskId") { type = NavType.IntType }
                )
            ) { backStackEntry ->

                TaskDetailScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Заглушка: Екран Сповіщень
            composable(Screen.BottomTab.Notifications.route) {
                com.example.coreops.ui.notifications.NotificationsScreen()
            }

            // Екран Профілю
            composable(Screen.BottomTab.Profile.route) {
                // Отримує  AuthViewModel
                val authViewModel: com.example.coreops.ui.auth.AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Мій Профіль",
                            fontSize = 24.sp,
                            color = androidx.compose.ui.graphics.Color.Black,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                authViewModel.logout()
                                onLogout()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Red)
                        ) {
                            Text("Вийти з акаунту", color = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                }
            }

            composable(
                route = Screen.CreateTask.route,
                arguments = listOf(
                    navArgument("projectId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getInt("projectId") ?: 0

                com.example.coreops.ui.tasks.CreateTaskScreen(
                    projectId = projectId,
                    onNavigateBack = { navController.popBackStack() },
                    onTaskCreated = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

package com.example.coreops.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add // Не забудь цей імпорт!
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coreops.data.remote.models.TaskDto
import com.example.coreops.ui.tasks.components.TaskCard

// STATEFUL ЕКРАН
@Composable
fun ProjectTasksScreen(
    projectId: Int,
    viewModel: ProjectTasksViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onTaskClick: (Int) -> Unit,
    onCreateTaskClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    ProjectTasksContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onTaskClick = onTaskClick,
        onCreateTaskClick = onCreateTaskClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectTasksContent(
    state: ProjectTasksState,
    onNavigateBack: () -> Unit,
    onTaskClick: (Int) -> Unit,
    onCreateTaskClick: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Задачі проєкту",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF3F4F6)
                )
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTaskClick,
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Додати задачу")
            }
        }
    ) { paddingValues ->
        // ... ТУТ ДАЛІ ТВІЙ BOX ТА ЛОГІКА СТАНІВ (нічого не міняй)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Обробка станів
            when (state) {
                is ProjectTasksState.Loading -> {
                    CircularProgressIndicator(
                        color = Color(0xFF2563EB),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ProjectTasksState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }

                is ProjectTasksState.Success -> {
                    val tasks = state.tasks

                    if (tasks.isEmpty()) {
                        Text(
                            text = "У цьому проєкті поки немає задач 📝",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(tasks) { task ->
                                TaskCard(
                                    task = task,
                                    onClick = { onTaskClick(task.id) },
                                    onStatusChange = { newStatus ->
                                        // Заглушка:  реалізую цей запит на бекенд у наступних кроках
                                        println("Зміна статусу на: $newStatus")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ProjectTasksScreenPreview() {
    val mockTasks = listOf(
        TaskDto(
            id = 1,
            title = "Налаштувати БД",
            description = "Створити таблиці",
            status = "todo",
            priority = "high",
            taskType = "task",
            assigneeName = "Іван",
            reporterName = "Адмін",
            projectName = "Project Alpha",
            dueDate = "Завтра",
            estimatedHours = 5f
        )
    )

    MaterialTheme {
        ProjectTasksContent(
            state = ProjectTasksState.Success(mockTasks),
            onNavigateBack = {},
            onTaskClick = {},
            onCreateTaskClick = {}
        )
    }
}
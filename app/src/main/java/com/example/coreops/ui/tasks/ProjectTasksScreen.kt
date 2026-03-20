package com.example.coreops.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coreops.ui.tasks.components.TaskCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectTasksScreen(
    projectId: Int, // Залишаємо для можливого використання в TopAppBar
    onNavigateBack: () -> Unit,
    onTaskClick: (Int) -> Unit,
    viewModel: ProjectTasksViewModel = hiltViewModel() // Інжектимо ViewModel
) {
    // Підписуємося на стан
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Задачі", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF3F4F6)
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val currentState = state) {
                is ProjectTasksState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF2563EB)
                    )
                }

                is ProjectTasksState.Success -> {
                    if (currentState.tasks.isEmpty()) {
                        Text(
                            text = "У цьому проєкті ще немає задач.\nЧас створити першу!",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF6B7280),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                        ) {
                            items(currentState.tasks) { task ->
                                TaskCard(
                                    task = task,
                                    onClick = {
                                        onTaskClick(task.id)
                                    },
                                    onStatusChange = { newStatus ->
                                        // Викликає метод з ViewModel
                                        viewModel.changeTaskStatus(task.id, newStatus)
                                    }
                                )
                            }
                        }
                    }
                }

                is ProjectTasksState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentState.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = { viewModel.loadTasks(projectId) }) {
                            Text("Спробувати ще раз")
                        }
                    }
                }
            }
        }
    }
}
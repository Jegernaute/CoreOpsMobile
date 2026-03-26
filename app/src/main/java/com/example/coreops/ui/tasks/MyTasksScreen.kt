package com.example.coreops.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coreops.ui.tasks.components.TaskCard




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTasksScreen(
    viewModel: MyTasksViewModel = hiltViewModel(),
    onTaskClick: (Int) -> Unit
) {
    val state by viewModel.state.collectAsState()

    val tabs = listOf("Усі", "До виконання", "В роботі", "На перевірці", "Виконано")
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мої задачі", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- 1. ТАБИ СТАТУСІВ ---
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                edgePadding = 16.dp,
                divider = {},
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTabIndex == index) Color(0xFF2563EB) else Color.Gray,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // --- 2. КОНТЕНТ ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (state) {
                    is MyTasksState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF2563EB)
                        )
                    }
                    is MyTasksState.Error -> {
                        val errorMessage = (state as MyTasksState.Error).message
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(errorMessage, color = Color.Red, modifier = Modifier.padding(16.dp))
                            Button(
                                onClick = { viewModel.fetchMyTasks() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                            ) {
                                Text("Спробувати ще раз")
                            }
                        }
                    }
                    is MyTasksState.Success -> {
                        val allTasks = (state as MyTasksState.Success).tasks


                        val filteredTasks = when (selectedTabIndex) {
                            1 -> allTasks.filter { it.status == "todo" }
                            2 -> allTasks.filter { it.status == "in_progress" }
                            3 -> allTasks.filter { it.status == "review" }
                            4 -> allTasks.filter { it.status == "done" }
                            else -> allTasks
                        }

                        if (filteredTasks.isEmpty()) {
                            Text(
                                text = "У цій категорії задач немає 🎉",
                                modifier = Modifier.align(Alignment.Center),
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredTasks) { task ->
                                    TaskCard(
                                        task = task,
                                        onClick = { onTaskClick(task.id) },
                                        onStatusChange = { newStatus -> // <--- Картка віддає лише статус

                                            // 1. Беремо ID прямо з поточного об'єкта task
                                            val currentTaskId = task.id

                                            // 2. Перетворюємо TaskStatus на рядок для API.
                                            // (Якщо у тебе є вбудована функція типу newStatus.toApiString(),
                                            // використай її. Якщо ні — ось надійний спосіб):
                                            val statusStr = when (newStatus.name) { // Припускаю, що TaskStatus - це Enum
                                                "TODO" -> "todo"
                                                "IN_PROGRESS" -> "in_progress"
                                                "REVIEW" -> "review"
                                                "DONE" -> "done"
                                                else -> newStatus.name.lowercase()
                                            }

                                            // 3. Відправляємо на бекенд
                                            viewModel.updateTaskStatus(currentTaskId, statusStr)
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
}
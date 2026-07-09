package com.example.coreops.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.coreops.ui.tasks.components.TaskCard
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.FilterAlt



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTasksScreen(
    viewModel: MyTasksViewModel = hiltViewModel(),
    onTaskClick: (Int) -> Unit,
    onCreateTaskClick: () -> Unit,
    onBackClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // СИНХРОНІЗАЦІЯ ДАНИХ: Оновлення при поверненні на екран
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.fetchMyTasks(isSilent = true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val tabs = listOf("Усі", "До виконання", "В роботі", "На перевірці", "Виконано")
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Мої задачі", fontSize = 20.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            modifier = Modifier.size(28.dp),
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Логіка фільтрів */ }) {
                        Icon(
                            imageVector = Icons.Outlined.FilterAlt,
                            contentDescription = "Фільтри",
                            modifier = Modifier.size(28.dp),
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF3F4F6))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTaskClick,
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White,
                shape = androidx.compose.foundation.shape.CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Додати",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = Color(0xFFF3F4F6)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // --- 1. ТАБИ СТАТУСІВ ---
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabs.size) { index ->
                    val isSelected = selectedTabIndex == index
                    Surface(
                        onClick = { selectedTabIndex = index },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                        color = if (isSelected) Color(0xFF2563EB) else Color.White,
                        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)) else null ,
                        modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                    ) {
                        Text(
                            text = tabs[index],
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            color = if (isSelected) Color.White else Color(0xFF4B5563),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
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
                                items(items = filteredTasks, key = { task -> task.id }) { task ->
                                    TaskCard(
                                        task = task,
                                        showProjectName = true,
                                        onClick = { onTaskClick(task.id) }
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
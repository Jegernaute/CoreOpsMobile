package com.example.coreops.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.coreops.data.remote.models.TaskDto
import com.example.coreops.ui.tasks.components.TaskCard

@Composable
fun ProjectTasksScreen(
    projectId: Int,
    viewModel: ProjectTasksViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onTaskClick: (Int) -> Unit,
    onCreateTaskClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // СИНХРОНІЗАЦІЯ ДАНИХ (Вирішує проблему з лічильником коментарів)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadTasks(projectId, isSilent = true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val tabs = listOf("Усі", "До виконання", "В роботі", "На перевірці", "Готово")
    var selectedTabIndex by remember { mutableStateOf(0) }

    ProjectTasksContent(
        state = state,
        tabs = tabs,
        selectedTabIndex = selectedTabIndex,
        onTabSelected = { selectedTabIndex = it },
        onNavigateBack = onNavigateBack,
        onTaskClick = onTaskClick,
        onCreateTaskClick = onCreateTaskClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectTasksContent(
    state: ProjectTasksState,
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    onTaskClick: (Int) -> Unit,
    onCreateTaskClick: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (state as? ProjectTasksState.Success)?.projectName ?: "Завантаження...",
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        (state as? ProjectTasksState.Success)?.activeSprintName?.let { sprintName ->
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = Color(0xFFE5E7EB),
                                        shape = RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = sprintName,
                                    color = Color(0xFF374151),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
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
                actions = {
                    IconButton(onClick = { /* TODO: Логіка фільтрів */ }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Фільтри",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF3F4F6))
                    .padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(tabs) { index, title ->
                    val isSelected = selectedTabIndex == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFF2563EB) else Color.White)
                            .clickable { onTabSelected(index) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) Color.White else Color(0xFF4B5563),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
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
                        val allTasks = state.tasks

                        val filteredTasks = when (selectedTabIndex) {
                            1 -> allTasks.filter { it.status == "to_do" }
                            2 -> allTasks.filter { it.status == "in_progress" }
                            3 -> allTasks.filter { it.status == "review" }
                            4 -> allTasks.filter { it.status == "done" }
                            else -> allTasks
                        }

                        if (filteredTasks.isEmpty()) {
                            Text(
                                text = "У цій категорії задач немає 📝",
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
                                items(items = filteredTasks, key = { task -> task.id }) { task ->
                                    TaskCard(
                                        task = task,
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

@Preview(showBackground = true)
@Composable
fun ProjectTasksScreenPreview() {
    val mockTasks = listOf(
        TaskDto(
            id = 1,
            title = "Оновити дизайн екрану задач",
            description = null,
            status = "to_do",
            priority = "high",
            taskType = "feature",
            assigneeName = "Іван",
            assigneeAvatar = null,
            reporterName = "Tech Lead",
            reporterAvatar = null,
            projectName = "CoreOps Mobile",
            projectKey = "MOB",
            commentsCount = 3,
            resourcesCount = 1,
            estimatedHours = 4.5f,
            dueDate = "2026-07-15",
            sprint = null
        )
    )

    MaterialTheme {
        ProjectTasksContent(
            state = ProjectTasksState.Success(mockTasks),
            tabs = listOf("Усі", "До виконання", "В роботі", "На перевірці", "Готово"),
            selectedTabIndex = 0,
            onTabSelected = {},
            onNavigateBack = {},
            onTaskClick = {},
            onCreateTaskClick = {}
        )
    }
}
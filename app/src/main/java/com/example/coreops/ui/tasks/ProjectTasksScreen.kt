package com.example.coreops.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.FilterAlt
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.coreops.data.remote.models.TaskDto
import com.example.coreops.ui.tasks.components.TaskCard
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectTasksScreen(
    projectId: Int,
    viewModel: ProjectTasksViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onTaskClick: (Int) -> Unit,
    onCreateTaskClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val currentFilters by viewModel.filters.collectAsState()
    val projectMembers by viewModel.projectMembers.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    var showFilterSheet by remember { mutableStateOf(false) }

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
        onCreateTaskClick = onCreateTaskClick,
        onFilterClick = { showFilterSheet = true }
    )

    if (showFilterSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        var tempPriority by remember { mutableStateOf(currentFilters.priority) }
        var tempTaskType by remember { mutableStateOf(currentFilters.taskType) }
        var tempDeadline by remember { mutableStateOf(currentFilters.deadlineFilter) }
        var tempAssignee by remember { mutableStateOf(currentFilters.assignee) }
        var tempReporter by remember { mutableStateOf(currentFilters.reporter) }

        var expandedAssigneeMenu by remember { mutableStateOf(false) }
        var expandedReporterMenu by remember { mutableStateOf(false) }

        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Фільтри проєкту",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Виконавець та Автор (Row)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Виконавець
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Виконавець", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Gray)
                        val selectedAssignee = projectMembers.find { it.userId == tempAssignee }?.userName ?: "Будь-хто"

                        ExposedDropdownMenuBox(
                            expanded = expandedAssigneeMenu,
                            onExpandedChange = { expandedAssigneeMenu = !expandedAssigneeMenu }
                        ) {
                            OutlinedTextField(
                                value = selectedAssignee,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAssigneeMenu) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedAssigneeMenu,
                                onDismissRequest = { expandedAssigneeMenu = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Будь-хто", color = Color.Black, fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        tempAssignee = null
                                        expandedAssigneeMenu = false
                                    }
                                )
                                HorizontalDivider()
                                projectMembers.forEach { member ->
                                    DropdownMenuItem(
                                        text = { Text(member.userName, color = Color.Black) },
                                        onClick = {
                                            tempAssignee = member.userId
                                            expandedAssigneeMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Автор
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Автор", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Gray)
                        val selectedReporter = projectMembers.find { it.userId == tempReporter }?.userName ?: "Будь-хто"

                        ExposedDropdownMenuBox(
                            expanded = expandedReporterMenu,
                            onExpandedChange = { expandedReporterMenu = !expandedReporterMenu }
                        ) {
                            OutlinedTextField(
                                value = selectedReporter,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedReporterMenu) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedReporterMenu,
                                onDismissRequest = { expandedReporterMenu = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Будь-хто", color = Color.Black, fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        tempReporter = null
                                        expandedReporterMenu = false
                                    }
                                )
                                HorizontalDivider()
                                projectMembers.forEach { member ->
                                    DropdownMenuItem(
                                        text = { Text(member.userName, color = Color.Black) },
                                        onClick = {
                                            tempReporter = member.userId
                                            expandedReporterMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Пріоритет
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Пріоритет", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Gray)
                    val priorities = listOf("low" to "Низький", "medium" to "Середній", "high" to "Високий", "critical" to "Критичний")

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        priorities.forEach { (backendValue, displayLabel) ->
                            val isSelected = tempPriority == backendValue
                            FilterChip(
                                selected = isSelected,
                                onClick = { tempPriority = if (isSelected) null else backendValue },
                                label = { Text(displayLabel) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFEFF6FF),
                                    selectedLabelColor = Color(0xFF2563EB)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true, selected = isSelected,
                                    borderColor = Color(0xFFE5E7EB), selectedBorderColor = Color(0xFF2563EB)
                                )
                            )
                        }
                    }
                }

                // Тип задачі
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Тип задачі", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Gray)
                    val taskTypes = listOf("task" to "Задача", "bug" to "Баг", "feature" to "Фіча")

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        taskTypes.forEach { (backendValue, displayLabel) ->
                            val isSelected = tempTaskType == backendValue
                            FilterChip(
                                selected = isSelected,
                                onClick = { tempTaskType = if (isSelected) null else backendValue },
                                label = { Text(displayLabel) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFEFF6FF),
                                    selectedLabelColor = Color(0xFF2563EB)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true, selected = isSelected,
                                    borderColor = Color(0xFFE5E7EB), selectedBorderColor = Color(0xFF2563EB)
                                )
                            )
                        }
                    }
                }

                // Термін виконання
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Термін виконання", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Gray)
                    val deadlines = listOf("today" to "На сьогодні", "week" to "На цьому тижні", "overdue" to "Протерміновані")

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        deadlines.forEach { (typeValue, displayLabel) ->
                            val isSelected = tempDeadline == typeValue
                            FilterChip(
                                selected = isSelected,
                                onClick = { tempDeadline = if (isSelected) null else typeValue },
                                label = { Text(displayLabel) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFEFF6FF),
                                    selectedLabelColor = Color(0xFF2563EB)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true, selected = isSelected,
                                    borderColor = Color(0xFFE5E7EB), selectedBorderColor = Color(0xFF2563EB)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Кнопки дій
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.clearFilters()
                            showFilterSheet = false
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
                    ) {
                        Text("Скинути")
                    }

                    Button(
                        onClick = {
                            val updatedFilters = currentFilters.copy(
                                priority = tempPriority,
                                taskType = tempTaskType,
                                deadlineFilter = tempDeadline,
                                assignee = tempAssignee,
                                reporter = tempReporter
                            )
                            viewModel.applyFilters(updatedFilters)
                            showFilterSheet = false
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Застосувати", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
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
    onCreateTaskClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ліва кнопка "Назад"
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(48.dp) // Стандартна зона натискання
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        modifier = Modifier.size(28.dp),
                        tint = Color.Black
                    )
                }

                // Центральний контент (Назва проєкту + Спринт)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = (state as? ProjectTasksState.Success)?.projectName ?: "Завантаження...",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    (state as? ProjectTasksState.Success)?.activeSprintName?.let { sprintName ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFFE5E7EB),
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = sprintName,
                                color = Color(0xFF4B5563),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Права кнопка "Фільтри"
                IconButton(
                    onClick = onFilterClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FilterAlt,
                        contentDescription = "Фільтри",
                        modifier = Modifier.size(28.dp),
                        tint = Color.Black
                    )
                }
            }
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
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(tabs) { index, title ->
                    val isSelected = selectedTabIndex == index
                    Surface(
                        onClick = { onTabSelected(index) },
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) Color(0xFF2563EB) else Color.White,
                        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)) else null,
                        modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = title,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                color = if (isSelected) Color.White else Color(0xFF4B5563),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
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
                                text = "У цій категорії задач немає ",
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
            onCreateTaskClick = {},
            onFilterClick = {}
        )
    }
}
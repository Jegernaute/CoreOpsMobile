package com.example.coreops.ui.tasks

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults


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

    // Збирає поточні активні фільтри з ViewModel
    val currentFilters by viewModel.filters.collectAsState()

    // Стан для відображення Bottom Sheet
    var showFilterSheet by remember { mutableStateOf(false) }

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // Асиметричний відступ: зменшує низ, щоб прибрати порожнечу
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Пустий простір зліва для симетрії
                Spacer(modifier = Modifier.width(48.dp))

                Text(
                    text = "Мої задачі",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                IconButton(
                    onClick = { showFilterSheet = true } // відкриває панель
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
        containerColor = Color(0xFFF3F4F6)
    )



    { paddingValues ->
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
                            1 -> allTasks.filter { it.status == "to_do" }
                            2 -> allTasks.filter { it.status == "in_progress" }
                            3 -> allTasks.filter { it.status == "review" }
                            4 -> allTasks.filter { it.status == "done" }
                            else -> allTasks
                        }

                        if (filteredTasks.isEmpty()) {
                            Text(
                                text = "У цій категорії задач немає ",
                                modifier = Modifier.align(Alignment.Center),
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
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
// --- 3. ПАНЕЛЬ ФІЛЬТРІВ (BOTTOM SHEET) ---
            if (showFilterSheet) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                // Отримує завантажені проєкти та учасників з ViewModel
                val projects by viewModel.projects.collectAsState()
                val members by viewModel.availableMembers.collectAsState()

                // Тимчасові змінні для UI
                var tempProject by remember { mutableStateOf(currentFilters.project) }
                var tempPriority by remember { mutableStateOf(currentFilters.priority) }
                var tempTaskType by remember { mutableStateOf(currentFilters.taskType) }
                var tempDeadline by remember { mutableStateOf(currentFilters.deadlineFilter) }
                var tempAssignee by remember { mutableStateOf(currentFilters.assignee) }
                var tempReporter by remember { mutableStateOf(currentFilters.reporter) }

                var expandedProjectMenu by remember { mutableStateOf(false) } // Стан для DropdownMenu
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
                            text = "Фільтри",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        // ==========================================
                        // СЕКЦІЯ 1: Вибір Проєкту
                        // ==========================================
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Проєкт", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Gray)

                            val selectedProjectName = projects.find { it.id == tempProject }?.name ?: "Всі проєкти"

                            ExposedDropdownMenuBox(
                                expanded = expandedProjectMenu,
                                onExpandedChange = { expandedProjectMenu = !expandedProjectMenu }
                            ) {
                                OutlinedTextField(
                                    value = selectedProjectName,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProjectMenu) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedProjectMenu,
                                    onDismissRequest = { expandedProjectMenu = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    // Опція для скидання фільтру (показати всі)
                                    DropdownMenuItem(
                                        text = { Text("Всі проєкти", color = Color.Black, fontWeight = FontWeight.Medium) },
                                        onClick = {
                                            tempProject = null
                                            expandedProjectMenu = false
                                        }
                                    )
                                    HorizontalDivider()
                                    // Динамічний список проєктів
                                    projects.forEach { project ->
                                        DropdownMenuItem(
                                            text = { Text(project.name, color = Color.Black) },
                                            onClick = {
                                                tempProject = project.id
                                                expandedProjectMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // ==========================================
                        // СЕКЦІЯ 2: Виконавець та Автор (Row)
                        // ==========================================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Виконавець
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Виконавець", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Gray)
                                val selectedAssignee = members.find { it.userId == tempAssignee }?.userName ?: "Будь-хто"

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
                                        members.forEach { member ->
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
                                val selectedReporter = members.find { it.userId == tempReporter }?.userName ?: "Будь-хто"

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
                                        members.forEach { member ->
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

                        // СЕКЦІЯ 3: Пріоритет
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
                                        onClick = {
                                            tempPriority = if (isSelected) null else backendValue
                                        },
                                        label = { Text(displayLabel) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFEFF6FF),
                                            selectedLabelColor = Color(0xFF2563EB)
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = Color(0xFFE5E7EB),
                                            selectedBorderColor = Color(0xFF2563EB)
                                        )
                                    )
                                }
                            }
                        }

                        // СЕКЦІЯ 4: Тип задачі
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
                                        onClick = {
                                            tempTaskType = if (isSelected) null else backendValue
                                        },
                                        label = { Text(displayLabel) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFEFF6FF),
                                            selectedLabelColor = Color(0xFF2563EB)
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = Color(0xFFE5E7EB),
                                            selectedBorderColor = Color(0xFF2563EB)
                                        )
                                    )
                                }
                            }
                        }

                        // СЕКЦІЯ 5: Терміни (Дедлайн)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Термін виконання", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Gray)
                            val deadlines = listOf(
                                "today" to "На сьогодні",
                                "week" to "На цьому тижні",
                                "overdue" to "Протерміновані"
                            )

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
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = Color(0xFFE5E7EB),
                                            selectedBorderColor = Color(0xFF2563EB)
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // КНОПКИ ДІЙ
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
                                        project = tempProject,
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
    }
}
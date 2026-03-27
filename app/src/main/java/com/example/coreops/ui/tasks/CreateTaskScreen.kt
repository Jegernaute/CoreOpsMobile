package com.example.coreops.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDateFromMillis(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date(millis))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    projectId: Int,
    viewModel: CreateTaskViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onTaskCreated: () -> Unit
) {
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val taskType by viewModel.taskType.collectAsState()
    val priority by viewModel.priority.collectAsState()

    val selectedAssigneeId by viewModel.selectedAssigneeId.collectAsState()
    val estimatedHours by viewModel.estimatedHours.collectAsState()
    val dueDate by viewModel.dueDate.collectAsState()

    val uiState by viewModel.uiState.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is CreateTaskState.Success) {
            onTaskCreated()
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Нова задача", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val projects by viewModel.projects.collectAsState()
            val selectedProjectId by viewModel.selectedProjectId.collectAsState()

            // ЗДОБУВАЄМО УЧАСНИКІВ ОБРАНОГО ПРОЄКТУ
            val currentProjectMembers = projects.find { it.id == selectedProjectId }?.members ?: emptyList()

            // --- ВИБІР ПРОЄКТУ (Показується тільки якщо зайшли з Моїх Задач) ---
            if (projectId == 0) {
                var expanded by remember { mutableStateOf(false) }
                val selectedProjectName = projects.find { it.id == selectedProjectId }?.name ?: "Оберіть проєкт"

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedProjectName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Проєкт *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        isError = uiState is CreateTaskState.Error && selectedProjectId == null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        if (projects.isEmpty()) {
                            DropdownMenuItem(text = { Text("Завантаження...") }, onClick = {})
                        } else {
                            projects.forEach { project ->
                                DropdownMenuItem(
                                    text = { Text(project.name, color = Color.Black) },
                                    onClick = {
                                        viewModel.setProject(project.id)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // --- ВИБІР ВИКОНАВЦЯ (Dropdown) ---
            var expandedAssignee by remember { mutableStateOf(false) }
            val selectedAssigneeName = currentProjectMembers.find { it.userId == selectedAssigneeId }?.userName ?: "Оберіть виконавця"

            ExposedDropdownMenuBox(
                expanded = expandedAssignee,
                onExpandedChange = {
                    // Відкриваємо список тільки якщо обрано проєкт
                    if (selectedProjectId != null) expandedAssignee = !expandedAssignee
                }
            ) {
                OutlinedTextField(
                    value = if (selectedProjectId == null) "Спочатку оберіть проєкт" else selectedAssigneeName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Виконавець") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAssignee) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    enabled = selectedProjectId != null, // Блокуємо, якщо проєкт не обрано
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledTextColor = Color.Gray
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedAssignee,
                    onDismissRequest = { expandedAssignee = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    if (currentProjectMembers.isEmpty()) {
                        DropdownMenuItem(text = { Text("В проєкті немає учасників", color = Color.Gray) }, onClick = {})
                    } else {
                        DropdownMenuItem(
                            text = { Text("Не призначати", color = Color.Gray) },
                            onClick = { viewModel.setAssignee(null); expandedAssignee = false }
                        )
                        HorizontalDivider()
                        currentProjectMembers.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.userName.takeIf { it.isNotBlank() } ?: member.userEmail, color = Color.Black) },
                                onClick = {
                                    viewModel.setAssignee(member.userId)
                                    expandedAssignee = false
                                }
                            )
                        }
                    }
                }
            }

            // --- НАЗВА ---
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.setTitle(it) },
                label = { Text("Назва задачі *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState is CreateTaskState.Error && title.isBlank(),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            // --- ОПИС ---
            OutlinedTextField(
                value = description,
                onValueChange = { viewModel.setDescription(it) },
                label = { Text("Опис (опціонально)") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            // --- ОЦІНКА ЧАСУ ---
            OutlinedTextField(
                value = estimatedHours,
                onValueChange = { viewModel.setEstimatedHours(it) },
                label = { Text("Оцінка (год)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            // --- ДЕДЛАЙН (КАЛЕНДАР) ---
            OutlinedTextField(
                value = dueDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("Дедлайн") },
                placeholder = { Text("Оберіть дату") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Календар")
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledTextColor = Color.Black
                ),
                enabled = false
            )

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState()
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            val selectedMillis = datePickerState.selectedDateMillis
                            if (selectedMillis != null) {
                                viewModel.setDueDate(formatDateFromMillis(selectedMillis))
                            }
                            showDatePicker = false
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Скасувати") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            // --- ТИП ЗАДАЧІ ---
            Text("Тип задачі", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            val taskTypes = listOf("task" to "Задача", "bug" to "Баг", "feature" to "Фіча")
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                taskTypes.forEach { (backendValue, displayLabel) ->
                    FilterChip(selected = taskType == backendValue, onClick = { viewModel.setTaskType(backendValue) }, label = { Text(displayLabel) })
                }
            }

            // --- ПРІОРИТЕТ ---
            Text("Пріоритет", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            val priorities = listOf("low" to "Низький", "medium" to "Середній", "high" to "Високий", "critical" to "Критичний")
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                priorities.forEach { (backendValue, displayLabel) ->
                    FilterChip(selected = priority == backendValue, onClick = { viewModel.setPriority(backendValue) }, label = { Text(displayLabel) })
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- ПОМИЛКА ---
            if (uiState is CreateTaskState.Error) {
                Text(
                    text = (uiState as CreateTaskState.Error).message,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // --- КНОПКА СТВОРЕННЯ ---
            Button(
                onClick = { viewModel.submitTask() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = uiState !is CreateTaskState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                if (uiState is CreateTaskState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Створити задачу", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
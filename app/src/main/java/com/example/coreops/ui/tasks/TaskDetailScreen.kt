package com.example.coreops.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coreops.data.remote.models.TaskDto

// 1. STATEFUL ЕКРАН (Підключений до ViewModel)
@Composable
fun TaskDetailScreen(
    viewModel: TaskDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    // Підписка на поточний стан
    val state by viewModel.state.collectAsState()

    TaskDetailContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onStatusChange = { newStatus ->
            // Викликаємо функцію з ViewModel для відправки на бекенд
            viewModel.updateStatus(newStatus)
        }
    )
}

// 2. STATELESS КОНТЕНТ
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailContent(
    state: TaskDetailState,
    onNavigateBack: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFF3F4F6), // Світло-сірий фон
        topBar = {
            TopAppBar(
                title = { Text("Деталі задачі", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF3F4F6)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Обробка станів
            when (state) {
                is TaskDetailState.Loading -> {
                    CircularProgressIndicator(
                        color = Color(0xFF2563EB),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is TaskDetailState.Error -> {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }

                is TaskDetailState.Success -> {
                    // Передаємо дані у функцію відмальовки деталей
                    TaskDetailBody(
                        task = state.task,
                        onStatusChange = onStatusChange
                    )
                }
            }
        }
    }
}

// 3. ТІЛО ЕКРАНУ (Малює саму задачу)
@Composable
fun TaskDetailBody(
    task: TaskDto,
    onStatusChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Заголовок
        Text(
            text = task.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        // Dropdown для зміни статусу
        StatusDropdown(
            currentStatus = task.status,
            onStatusChange = onStatusChange
        )

        // Блок з деталями (на білому фоні)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Опис:", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = task.description ?: "Опис відсутній", color = Color.Black)

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFE5E7EB))
                Spacer(modifier = Modifier.height(16.dp))

                // Інші дані
                DetailRow(label = "Проєкт:", value = task.projectName)
                DetailRow(label = "Виконавець:", value = task.assigneeName ?: "Не призначено")
                DetailRow(label = "Автор:", value = task.reporterName)
                DetailRow(label = "Пріоритет:", value = task.priority.uppercase())
                DetailRow(label = "Дедлайн:", value = task.dueDate ?: "Не вказано")
            }
        }
    }
}

// Допоміжний рядок для деталей
@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

// 4. КОМПОНЕНТ DROPDOWN MENU ДЛЯ СТАТУСУ
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusDropdown(
    currentStatus: String,
    onStatusChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Словник статусів: Ключ для бекенду -> Текст для UI
    val statuses = listOf(
        "todo" to "До виконання",
        "in_progress" to "В процесі",
        "review" to "На перевірці",
        "done" to "Готово"
    )

    // Шукаємо красиву назву для поточного статусу
    val currentLabel = statuses.find { it.first == currentStatus }?.second ?: currentStatus

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = currentLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Статус задачі") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            statuses.forEach { (backendValue, displayLabel) ->
                DropdownMenuItem(
                    text = { Text(displayLabel, color = Color.Black) },
                    onClick = {
                        // Відправляємо новий статус у ViewModel
                        onStatusChange(backendValue)
                        expanded = false
                    }
                )
            }
        }
    }
}
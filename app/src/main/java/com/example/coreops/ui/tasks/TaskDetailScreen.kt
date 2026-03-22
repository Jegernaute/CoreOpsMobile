package com.example.coreops.ui.tasks

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coreops.data.remote.models.TaskDto
import com.example.coreops.domain.model.TaskStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current // Отримує контекст для Toast

    // Слухає одноразові події з ViewModel
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { message ->
            // Показує спливаюче повідомлення внизу екрану
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Деталі задачі", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF3F4F6) // Базовий фон додатку
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val currentState = state) {
                is TaskDetailState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF2563EB)
                    )
                }

                is TaskDetailState.Error -> {
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
                        // Тут можна додати кнопку "Спробувати ще раз" якщо потрібно
                    }
                }

                is TaskDetailState.Success -> {
                    TaskDetailContent(
                        task = currentState.task,
                        onStatusChange = { newStatus ->
                            viewModel.updateStatus(newStatus)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskDetailContent(task: TaskDto,
                      onStatusChange: (TaskStatus) -> Unit) {
    // Стан для відкриття меню
    var expanded by remember { mutableStateOf(false) }

    // Використовує verticalScroll щоб довгий опис можна було гортати
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // --- 1. Заголовок ---
        Text(
            text = task.title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // --- 2. Бейджі (Статус, Пріоритет, Тип) ---
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val statusObj = TaskStatus.fromApiValue(task.status)
            // Інтерактивний бейдж статусу
            Box {
                // Сама кнопка
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEFF6FF),
                    contentColor = Color(0xFF2563EB),
                    modifier = Modifier.clickable { expanded = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = statusObj.displayName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Змінити статус",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Випадаюче меню
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    TaskStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = status.displayName,
                                    color = if (status == statusObj) Color(0xFF2563EB) else Color(0xFF111827),
                                    fontWeight = if (status == statusObj) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                expanded = false
                                if (status != statusObj) {
                                    onStatusChange(status)
                                }
                            }
                        )
                    }
                }
            }

            //  бейдж пріоритету
            DetailBadge(
                text = task.priority.uppercase(),
                containerColor = Color(0xFFF3F4F6),
                textColor = getDetailPriorityColor(task.priority)
            )

            //  бейдж  типу задачі
            DetailBadge(
                text = task.taskType,
                containerColor = Color(0xFFEFF6FF),
                textColor = Color(0xFF2563EB)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 3. Інформаційна картка (Люди та Дати) ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow(label = "Проєкт", value = task.projectName)
                Divider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(vertical = 8.dp))

                InfoRow(label = "Виконавець", value = task.assigneeName ?: "Не призначено")
                Divider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(vertical = 8.dp))

                InfoRow(label = "Автор", value = task.reporterName)

                // Виводимо дати і години, тільки якщо вони є
                if (task.dueDate != null) {
                    Divider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(vertical = 8.dp))
                    InfoRow(label = "Дедлайн", value = task.dueDate)
                }
                if (task.estimatedHours != null) {
                    Divider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(vertical = 8.dp))
                    InfoRow(label = "Оцінка часу", value = "${task.estimatedHours} год")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 4. Опис задачі ---
        Text(
            text = "Опис",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (!task.description.isNullOrBlank()) task.description else "Опис відсутній.",
                modifier = Modifier.padding(16.dp),
                color = if (!task.description.isNullOrBlank()) Color(0xFF374151) else Color(0xFF9CA3AF),
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        }

        // Відступ знизу
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// --- Допоміжні компоненти ---

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFF6B7280), fontSize = 14.sp)
        Text(text = value, color = Color(0xFF111827), fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DetailBadge(text: String, containerColor: Color, textColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color = containerColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

private fun getDetailStatusColor(status: String): Color = when (status) {
    "to_do" -> Color(0xFF6B7280)
    "in_progress" -> Color(0xFFF59E0B)
    "done" -> Color(0xFF10B981)
    else -> Color(0xFF6B7280)
}

private fun getDetailPriorityColor(priority: String): Color = when (priority) {
    "high", "critical" -> Color(0xFFEF4444)
    "medium" -> Color(0xFFF59E0B)
    "low" -> Color(0xFF10B981)
    else -> Color(0xFF6B7280)
}
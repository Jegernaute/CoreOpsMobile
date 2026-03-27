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
import com.example.coreops.data.remote.models.CommentDto
import com.example.coreops.data.remote.models.TaskDto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Schedule
@Composable
fun TaskDetailScreen(
    viewModel: TaskDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val isSending by viewModel.isSendingComment.collectAsState()

    TaskDetailContent(
        state = state,
        isSending = isSending,
        onNavigateBack = onNavigateBack,
        onStatusChange = { newStatus -> viewModel.updateStatus(newStatus) },
        onSendComment = { text -> viewModel.sendComment(text) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailContent(
    state: TaskDetailState,
    isSending: Boolean,
    onNavigateBack: () -> Unit,
    onStatusChange: (String) -> Unit,
    onSendComment: (String) -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = { Text("Деталі задачі", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF3F4F6))
            )
        },

        bottomBar = {
            if (state is TaskDetailState.Success) {
                CommentInputBar(
                    isSending = isSending,
                    onSendComment = onSendComment
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                is TaskDetailState.Success -> {
                    TaskDetailBody(
                        task = state.task,
                        comments = state.comments,
                        onStatusChange = onStatusChange
                    )
                }
            }
        }
    }
}

@Composable
fun TaskDetailBody(
    task: TaskDto,
    comments: List<CommentDto>,
    onStatusChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- БЛОК 1: НАЗВА ТА СТАТУС ---
        Text(
            text = task.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        StatusDropdown(
            currentStatus = task.status,
            onStatusChange = onStatusChange
        )

        // =======================================================
        // 👇 НОВИЙ БЛОК ПЛАНУВАННЯ (З'являється тільки якщо є дані) 👇
        // =======================================================
        if (task.dueDate != null || task.sprint != null || task.estimatedHours != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE)), // Легкий блакитний фон
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Планування", fontWeight = FontWeight.Bold, color = Color(0xFF0284C7), fontSize = 14.sp)

                    // Спринт
                    if (task.sprint != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsRun, contentDescription = "Sprint", modifier = Modifier.size(18.dp), tint = Color(0xFF0284C7))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Спринт #${task.sprint}", fontSize = 14.sp, color = Color.Black)
                        }
                    }

                    // Дедлайн
                    if (task.dueDate != null) {
                        val formattedDate = task.dueDate.take(10) // YYYY-MM-DD
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = "Deadline", modifier = Modifier.size(18.dp), tint = Color.Red.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Дедлайн: $formattedDate", fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                        }
                    }

                    // Оцінка та витрачений час
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (task.estimatedHours != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = "Estimate", modifier = Modifier.size(18.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Оцінка: ${task.estimatedHours} год", fontSize = 14.sp, color = Color.Black)
                            }
                        }
                        if (task.actualHours != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Actual", modifier = Modifier.size(18.dp), tint = Color(0xFF10B981))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Витрачено: ${task.actualHours} год", fontSize = 14.sp, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
        // =======================================================
        // 👆 КІНЕЦЬ БЛОКУ ПЛАНУВАННЯ 👆
        // =======================================================

        // --- БЛОК 2: ОСНОВНІ ДЕТАЛІ ТА ОПИС ---
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

                DetailRow(label = "Проєкт:", value = task.projectName)
                DetailRow(label = "Виконавець:", value = task.assigneeName ?: "Не призначено")
                DetailRow(label = "Автор:", value = task.reporterName)
                DetailRow(label = "Пріоритет:", value = task.priority.uppercase())
            }
        }

        // --- БЛОК 3: КОМЕНТАРІ ---
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Коментарі (${comments.size})",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        if (comments.isEmpty()) {
            Text(
                text = "Поки немає коментарів. Напишіть першим!",
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            comments.forEach { comment ->
                CommentItem(comment = comment)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun CommentItem(comment: CommentDto) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = comment.authorName ?: "Невідомий автор",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = comment.createdAt.take(10),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = comment.content, fontSize = 14.sp, color = Color.DarkGray)
        }
    }
}

@Composable
fun CommentInputBar(
    isSending: Boolean,
    onSendComment: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Написати коментар...", color = Color.Gray) },
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF3F4F6),
                    unfocusedContainerColor = Color(0xFFF3F4F6),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onSendComment(text)
                        text = ""
                    }
                },
                enabled = !isSending && text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Надіслати", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusDropdown(
    currentStatus: String,
    onStatusChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val statuses = listOf(
        "todo" to "До виконання",
        "in_progress" to "В процесі",
        "review" to "На перевірці",
        "done" to "Готово"
    )
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
            modifier = Modifier.menuAnchor().fillMaxWidth()
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
                        onStatusChange(backendValue)
                        expanded = false
                    }
                )
            }
        }
    }
}
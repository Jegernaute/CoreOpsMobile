package com.example.coreops.ui.tasks.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coreops.data.remote.models.TaskDto
import com.example.coreops.domain.model.TaskStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: TaskDto,
    onClick: () -> Unit,
    onStatusChange: (TaskStatus) -> Unit // Новий колбек
) {
    // Стан для відкриття/закриття меню
    var expanded by remember { mutableStateOf(false) }
    // Поточний статус через  Enum
    val currentStatus = TaskStatus.fromApiValue(task.status)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Заголовок задачі
            Text(
                text = task.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Рядок з бейджами
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Статус
                // Інтерактивний статус
                Box {
                    TaskBadge(
                        text = currentStatus.displayName,
                        containerColor = getStatusColor(task.status),
                        textColor = Color.White,
                        modifier = Modifier.clickable { expanded = true } // Відкриває меню
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        // Перебирає всі статуси з Enum
                        TaskStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.displayName) },
                                onClick = {
                                    expanded = false // Ховаємо меню
                                    // Відправляє запит тільки якщо статус реально змінився
                                    if (status != currentStatus) {
                                        onStatusChange(status)
                                    }
                                }
                            )
                        }
                    }
                }

                // Пріоритет
                TaskBadge(
                    text = task.priority.uppercase(),
                    containerColor = Color(0xFFF3F4F6),
                    textColor = getPriorityColor(task.priority)
                )

                // Тип задачі
                TaskBadge(
                    text = task.taskType,
                    containerColor = Color(0xFFEFF6FF),
                    textColor = Color(0xFF2563EB)
                )
            }
        }
    }
}

// --- Допоміжні компоненти для красивого UI ---

@Composable
private fun TaskBadge(text: String, containerColor: Color, textColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color = containerColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)

    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

// Проста логіка кольорів
private fun getStatusColor(status: String): Color = when (status) {
    "to_do" -> Color(0xFF6B7280) // Сірий
    "in_progress" -> Color(0xFFF59E0B) // Жовтий
    "done" -> Color(0xFF10B981) // Зелений
    else -> Color(0xFF6B7280)
}

private fun formatStatus(status: String): String = when (status) {
    "to_do" -> "To Do"
    "in_progress" -> "In Progress"
    "done" -> "Done"
    else -> status
}

private fun getPriorityColor(priority: String): Color = when (priority) {
    "high" -> Color(0xFFEF4444) // Червоний
    "medium" -> Color(0xFFF59E0B) // Оранжевий
    "low" -> Color(0xFF10B981) // Зелений
    else -> Color(0xFF6B7280)
}
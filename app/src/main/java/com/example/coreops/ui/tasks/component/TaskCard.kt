package com.example.coreops.ui.tasks.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coreops.data.remote.models.TaskDto

@Composable
fun TaskCard(
    task: TaskDto,
    showProjectName: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // --- 1. Шапка: Тип задачі, Ключ, Лічильники ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Іконка типу задачі
                Icon(
                    imageVector = getTaskTypeIcon(task.taskType),
                    contentDescription = "Тип",
                    tint = getTaskTypeColor(task.taskType),
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Ідентифікатор та опціонально Назва проєкту
                val headerText = if (showProjectName && !task.projectName.isNullOrBlank()) {
                    "${task.projectKey}-${task.id} • ${task.projectName}"
                } else {
                    "${task.projectKey}-${task.id}"
                }

                Text(
                    text = headerText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4B5563),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Лічильники
                if (task.resourcesCount > 0) {
                    CounterBadge(icon = Icons.Outlined.AttachFile, count = task.resourcesCount)
                    Spacer(modifier = Modifier.width(12.dp))
                }

                if (task.commentsCount > 0) {
                    CounterBadge(icon = Icons.Outlined.ChatBubbleOutline, count = task.commentsCount)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- 2. Тіло: Назва ---
            Text(
                text = task.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- 3. Підвал: Пріоритет, Дата, Аватар ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Пріоритет
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(getPriorityBgColor(task.priority), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Flag,
                        contentDescription = "Пріоритет",
                        tint = getPriorityTextColor(task.priority),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.priority.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = getPriorityTextColor(task.priority)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Дата
                Text(
                    text = formatDate(task.dueDate),
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.weight(1f))

                // Аватар виконавця (або автора, якщо виконавця немає)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFE5E7EB), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (task.assigneeName ?: task.reporterName ?: "?").take(1).uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151)
                    )
                }
            }
        }
    }
}

@Composable
private fun CounterBadge(icon: ImageVector, count: Int) {
    val displayCount = if (count > 99) "99+" else count.toString()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = displayCount,
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}

// --- Утиліти мапінгу ---

private fun getTaskTypeIcon(type: String): ImageVector = when (type.lowercase()) {
    "bug" -> Icons.Outlined.BugReport
    "feature" -> Icons.Outlined.CheckCircle
    else -> Icons.Outlined.List
}

private fun getTaskTypeColor(type: String): Color = when (type.lowercase()) {
    "bug" -> Color(0xFFEF4444)
    "feature" -> Color(0xFF8B5CF6)
    else -> Color(0xFF3B82F6)
}

private fun getPriorityBgColor(priority: String): Color = when (priority.lowercase()) {
    "critical", "high" -> Color(0xFFFEE2E2)
    "medium" -> Color(0xFFFEF3C7)
    else -> Color(0xFFF3F4F6)
}

private fun getPriorityTextColor(priority: String): Color = when (priority.lowercase()) {
    "critical", "high" -> Color(0xFFDC2626)
    "medium" -> Color(0xFFD97706)
    else -> Color(0xFF4B5563)
}

// НОВА УТИЛІТА ДЛЯ ФОРМАТУВАННЯ ДАТИ
private fun formatDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "Без дати"
    return try {
        val parts = dateString.take(10).split("-")
        if (parts.size == 3) {
            val day = parts[2].toInt().toString()
            val month = when (parts[1]) {
                "01" -> "січ."
                "02" -> "лют."
                "03" -> "бер."
                "04" -> "квіт."
                "05" -> "трав."
                "06" -> "черв."
                "07" -> "лип."
                "08" -> "серп."
                "09" -> "вер."
                "10" -> "жовт."
                "11" -> "лист."
                "12" -> "груд."
                else -> ""
            }
            "$day $month"
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F4F6, name = "1. Bug / Critical / Вкладення")
@Composable
fun PreviewTaskCardBug() {
    val mockTask = TaskDto(
        id = 12,
        taskKey = "CORE-12",
        title = "Критична вразливість в модулі авторизації",
        description = null,
        status = "to_do",
        priority = "critical",
        taskType = "bug",
        assigneeDetails = null,
        reporterDetails = null,
        checklist = emptyList(),
        resources = emptyList(),
        assigneeName = "Олександр",
        assigneeAvatar = null,
        reporterName = "QA",
        reporterAvatar = null,
        projectName = "CoreOps",
        projectKey = "CORE",
        commentsCount = 14,
        resourcesCount = 3,
        estimatedHours = 4f,
        dueDate = "2026-07-01",
        sprintName = null
    )
    Box(modifier = Modifier.padding(16.dp)) {
        TaskCard(task = mockTask, onClick = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F4F6, name = "2. Feature / Medium / Без ресурсів")
@Composable
fun PreviewTaskCardFeature() {
    val mockTask = TaskDto(
        id = 45,
        taskKey = "CORE-45",
        title = "Додати підтримку темної теми",
        description = null,
        status = "in_progress",
        priority = "medium",
        taskType = "feature",
        assigneeDetails = null,
        reporterDetails = null,
        checklist = emptyList(),
        resources = emptyList(),
        assigneeName = null,
        assigneeAvatar = null,
        reporterName = "Марія",
        reporterAvatar = null,
        projectName = "CoreOps",
        projectKey = "CORE",
        commentsCount = 5,
        resourcesCount = 0,
        estimatedHours = 12f,
        dueDate = "2026-07-15",
        sprintName = "Sprint 2"
    )
    Box(modifier = Modifier.padding(16.dp)) {
        TaskCard(task = mockTask, onClick = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F4F6, name = "3. Task / Low / Ліміт лічильника")
@Composable
fun PreviewTaskCardTask() {
    val mockTask = TaskDto(
        id = 89,
        taskKey = "CORE-89",
        title = "Оновити документацію API до версії 2.0",
        description = null,
        status = "done",
        priority = "low",
        taskType = "task",
        assigneeDetails = null,
        reporterDetails = null,
        checklist = emptyList(),
        resources = emptyList(),
        assigneeName = "Іван",
        assigneeAvatar = null,
        reporterName = "Tech Lead",
        reporterAvatar = null,
        projectName = "CoreOps",
        projectKey = "CORE",
        commentsCount = 105,
        resourcesCount = 0,
        estimatedHours = 8f,
        dueDate = null,
        sprintName = null
    )
    Box(modifier = Modifier.padding(16.dp)) {
        TaskCard(task = mockTask, onClick = {})
    }
}